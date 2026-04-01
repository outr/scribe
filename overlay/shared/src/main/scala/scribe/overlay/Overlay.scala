package scribe.overlay

import scribe.{ANSI, Logger}
import scribe.handler.LogHandler

import java.io.PrintStream
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import scala.jdk.CollectionConverters._

sealed trait Overlay {
  def entry(initialText: String = ""): OverlayEntry
  def progressBar(label: String = "", width: Int = 40): OverlayEntry

  /**
   * Show a floating banner at an arbitrary screen position.
   * Activates scroll region mode to keep the banner fixed while logs scroll.
   * Works in standard terminals; not supported in IDE terminal emulators.
   */
  def banner(content: String, position: BannerPosition = BannerPosition.Center): Unit
  def clearBanner(): Unit
  def size: Int
  def dispose(): Unit
}

object Overlay {
  private val active = new AtomicReference[Option[Overlay]](None)

  def create(): Overlay = {
    val isTTY = System.console() != null || sys.env.contains("TERM")
    if (!isTTY) {
      val overlay = new NoOpOverlay
      active.set(Some(overlay))
      return overlay
    }
    val terminal = new Terminal
    val overlay = new ActiveOverlay(terminal)
    if (!active.compareAndSet(None, Some(overlay))) {
      throw new IllegalStateException("An overlay is already active. Call dispose() on the existing overlay first.")
    }
    overlay.activate()
    overlay
  }

  def current: Option[Overlay] = active.get()
  private[overlay] def clear(): Unit = active.set(None)
}

/**
 * Two rendering modes:
 *
 * 1. **Relative mode** (no banner): entries drawn using relative cursor movement
 *    (CursorUp/Down). Works in all terminals including IntelliJ.
 *
 * 2. **Scroll region mode** (with banner): a scroll region confines log output,
 *    entries and banner are drawn at absolute positions outside the scroll region.
 *    Works in standard terminals only.
 */
class ActiveOverlay private[overlay](terminal: Terminal) extends Overlay {
  private val entries = new CopyOnWriteArrayList[OverlayEntry]()
  private val lock = new AnyRef
  private var disposed = false
  private var savedOut: PrintStream = _
  private var savedErr: PrintStream = _
  private var originalHandlers: List[LogHandler] = Nil
  private var inOperation = false
  private val pendingText = new java.util.concurrent.ConcurrentLinkedQueue[String]()
  private var linesOnScreen: Int = 0

  // Banner state
  private var bannerContent: Option[String] = None
  private var bannerPos: BannerPosition = BannerPosition.Center
  private var scrollRegionActive = false

  private[overlay] def activate(): Unit = lock.synchronized {
    savedOut = System.out
    savedErr = System.err
    System.setOut(new OverlayStream(this))
    System.setErr(new OverlayStream(this))

    originalHandlers = Logger.root.handlers
    val wrappedHandlers = originalHandlers.map {
      case builder: scribe.handler.LogHandlerBuilder =>
        builder.copy(writer = new OverlayWriter(this, builder.writer))
      case other => other
    }
    Logger.root.copy(handlers = wrappedHandlers).replace()
    terminal.write(ANSI.ctrl.HideCursor)
  }

  override def entry(initialText: String = ""): OverlayEntry = {
    val e = new OverlayEntry(this, progressWidth = 40)
    entries.add(e)
    if (initialText.nonEmpty) e.update(initialText) else redraw()
    e
  }

  override def progressBar(label: String = "", width: Int = 40): OverlayEntry = {
    val e = new OverlayEntry(this, progressWidth = width)
    entries.add(e)
    e.update(0, label)
    e
  }

  override def size: Int = entries.size()

  override def banner(content: String, position: BannerPosition = BannerPosition.Center): Unit = lock.synchronized {
    bannerContent = Some(content)
    bannerPos = position
    if (!scrollRegionActive) activateScrollRegion()
    if (!inOperation) {
      inOperation = true
      try { doRedraw() } finally { inOperation = false }
    }
  }

  override def clearBanner(): Unit = lock.synchronized {
    if (bannerContent.isDefined) {
      // Erase the banner from screen before clearing state
      val (rows, cols) = terminal.size()
      val sb = new StringBuilder(256)
      eraseBannerAbsolute(sb, rows, cols, linesOnScreen)
      terminal.write(sb.toString())

      bannerContent = None
      deactivateScrollRegion()
      if (!inOperation) {
        inOperation = true
        try { doRedraw() } finally { inOperation = false }
      }
    }
  }

  private[overlay] def removeEntry(entry: OverlayEntry): Unit = {
    entries.remove(entry)
    redraw()
  }

  // ── Core operations ────────────────────────────────────────────────────

  private[overlay] def writeToScrollRegion(text: String): Unit = lock.synchronized {
    if (disposed) { terminal.write(text); return }
    if (inOperation) { pendingText.add(text); return }
    inOperation = true
    try {
      doWriteToScrollRegion(text)
      var pending = pendingText.poll()
      while (pending != null) { doWriteToScrollRegion(pending); pending = pendingText.poll() }
    } finally { inOperation = false }
  }

  private[overlay] def redraw(): Unit = lock.synchronized {
    if (disposed) return
    if (inOperation) return
    inOperation = true
    try {
      doRedraw()
      var pending = pendingText.poll()
      while (pending != null) { doWriteToScrollRegion(pending); pending = pendingText.poll() }
    } finally { inOperation = false }
  }

  private def doWriteToScrollRegion(text: String): Unit = {
    val (_, cols) = terminal.size()
    val truncated = truncateLines(text, cols)
    if (scrollRegionActive) doWriteScrollRegionMode(truncated)
    else doWriteRelativeMode(truncated)
  }

  private def doRedraw(): Unit = {
    if (scrollRegionActive) doRedrawScrollRegionMode()
    else doRedrawRelativeMode()
  }

  // ── Relative mode (no banner — works everywhere) ──────────────────────

  private def doWriteRelativeMode(text: String): Unit = {
    val entryList = entries.asScala.toList
    val sb = new StringBuilder(1024)
    appendEraseRelative(sb, linesOnScreen)
    sb.append(text)
    linesOnScreen = appendEntriesRelative(sb, entryList)
    terminal.write(sb.toString())
  }

  private def doRedrawRelativeMode(): Unit = {
    val entryList = entries.asScala.toList
    val sb = new StringBuilder(1024)
    appendEraseRelative(sb, linesOnScreen)
    linesOnScreen = appendEntriesRelative(sb, entryList)
    terminal.write(sb.toString())
  }

  private def appendEraseRelative(sb: StringBuilder, n: Int): Unit = {
    for (_ <- 0 until n) {
      sb.append(ANSI.ctrl.CursorUp(1))
      sb.append("\r")
      sb.append(ANSI.ctrl.Reset)
      sb.append(ANSI.ctrl.EraseLineEnd)
    }
  }

  private def appendEntriesRelative(sb: StringBuilder, entryList: List[OverlayEntry]): Int = {
    val (_, cols) = terminal.size()
    var totalLines = 0
    for (entry <- entryList) {
      for (line <- entry.currentContent.split("\n", -1)) {
        sb.append("\r")
        sb.append(ANSI.ctrl.Reset)
        sb.append(ANSI.ctrl.EraseLineEnd)
        sb.append(truncateAnsi(line, cols))
        sb.append(ANSI.ctrl.Reset)
        sb.append("\n")
        totalLines += 1
      }
    }
    totalLines
  }

  // ── Scroll region mode (with banner — standard terminals only) ────────

  private def activateScrollRegion(): Unit = {
    val (rows, _) = terminal.size()
    val entryLines = countEntryLines(entries.asScala.toList)
    // If in relative mode, erase existing relative entries first
    if (linesOnScreen > 0) {
      val sb = new StringBuilder(256)
      appendEraseRelative(sb, linesOnScreen)
      terminal.write(sb.toString())
      linesOnScreen = 0
    }
    val scrollBottom = rows - entryLines
    terminal.write(ANSI.ctrl.SetScrollRegion(1, scrollBottom))
    terminal.write(ANSI.ctrl.CursorMove(scrollBottom, 1))
    scrollRegionActive = true
  }

  private def deactivateScrollRegion(): Unit = {
    terminal.write(ANSI.ctrl.ResetScrollRegion)
    scrollRegionActive = false
    // Redraw entries in relative mode
    val (rows, _) = terminal.size()
    terminal.write(ANSI.ctrl.CursorMove(rows, 1))
    terminal.write("\n")
    linesOnScreen = 0
  }

  private def doWriteScrollRegionMode(text: String): Unit = {
    val (rows, cols) = terminal.size()
    val entryList = entries.asScala.toList
    val entryLines = countEntryLines(entryList)
    val scrollBottom = rows - entryLines

    val sb = new StringBuilder(2048)

    // Update scroll region if entry count changed
    sb.append(ANSI.ctrl.SetScrollRegion(1, scrollBottom))

    // Erase banner before writing log text — scrolling would push garbage through it
    eraseBannerAbsolute(sb, rows, cols, linesOnScreen)

    // Write log text in the scroll region (may cause scrolling)
    sb.append(ANSI.ctrl.CursorMove(scrollBottom, 1))
    sb.append(text)

    // Draw entries at absolute positions below the scroll region
    drawEntriesAbsolute(sb, rows, cols, entryList)

    // Redraw banner on top
    drawBannerAbsolute(sb, rows, cols)

    linesOnScreen = entryLines
    terminal.write(sb.toString())
  }

  private def doRedrawScrollRegionMode(): Unit = {
    val (rows, cols) = terminal.size()
    val entryList = entries.asScala.toList
    val entryLines = countEntryLines(entryList)
    val scrollBottom = rows - entryLines

    val sb = new StringBuilder(1024)

    // Update scroll region
    sb.append(ANSI.ctrl.SetScrollRegion(1, scrollBottom))

    // Erase banner first (it's inside the scroll region, so it would scroll with content)
    eraseBannerAbsolute(sb, rows, cols, linesOnScreen)

    // Redraw entries
    drawEntriesAbsolute(sb, rows, cols, entryList)

    // Redraw banner
    drawBannerAbsolute(sb, rows, cols)

    linesOnScreen = entryLines
    terminal.write(sb.toString())
  }

  private def drawEntriesAbsolute(sb: StringBuilder, rows: Int, cols: Int,
                                  entryList: List[OverlayEntry]): Unit = {
    val entryLines = countEntryLines(entryList)
    var row = rows - entryLines + 1
    for (entry <- entryList) {
      for (line <- entry.currentContent.split("\n", -1)) {
        if (row >= 1 && row <= rows) {
          sb.append(ANSI.ctrl.CursorMove(row, 1))
          sb.append(ANSI.ctrl.Reset)
          sb.append(ANSI.ctrl.EraseLineEnd)
          sb.append(truncateAnsi(line, cols))
          sb.append(ANSI.ctrl.Reset)
        }
        row += 1
      }
    }
  }

  /**
   * Erase the banner at the position it was LAST drawn.
   * Uses `forEntryLines` to calculate the scroll bottom — pass `linesOnScreen`
   * (the entry count from the last draw) rather than the current entry count.
   */
  private def eraseBannerAbsolute(sb: StringBuilder, rows: Int, cols: Int,
                                  forEntryLines: Int = -1): Unit = {
    bannerContent match {
      case None =>
      case Some(content) =>
        val contentLines = content.split("\n", -1)
        val plainLines = contentLines.map(stripAnsi)
        val maxContentWidth = if (plainLines.isEmpty) 0 else plainLines.map(_.length).max
        val boxWidth = maxContentWidth + 4
        val boxHeight = contentLines.length + 2
        val el = if (forEntryLines >= 0) forEntryLines else countEntryLines(entries.asScala.toList)
        val scrollBottom = rows - el

        val anchorRow = bannerPos.vertical match {
          case VerticalPosition.Top => 1
          case VerticalPosition.Center => math.max(1, (scrollBottom - boxHeight) / 2 + 1)
          case VerticalPosition.Bottom => math.max(1, scrollBottom - boxHeight + 1)
        }
        val startRow = math.max(1, math.min(scrollBottom - boxHeight + 1, anchorRow + bannerPos.rowOffset))
        val anchorCol = bannerPos.horizontal match {
          case HorizontalPosition.Left => 1
          case HorizontalPosition.Center => math.max(1, (cols - boxWidth) / 2 + 1)
          case HorizontalPosition.Right => math.max(1, cols - boxWidth + 1)
        }
        val startCol = math.max(1, math.min(cols - boxWidth + 1, anchorCol + bannerPos.colOffset))
        val blank = " " * boxWidth
        sb.append(ANSI.ctrl.Reset)
        for (row <- startRow until startRow + boxHeight) {
          if (row >= 1 && row <= scrollBottom) {
            sb.append(ANSI.ctrl.CursorMove(row, startCol))
            sb.append(blank)
          }
        }
    }
  }

  private def drawBannerAbsolute(sb: StringBuilder, rows: Int, cols: Int): Unit = {
    bannerContent match {
      case None =>
      case Some(content) =>
        val contentLines = content.split("\n", -1)
        val plainLines = contentLines.map(stripAnsi)
        val maxContentWidth = if (plainLines.isEmpty) 0 else plainLines.map(_.length).max
        val boxWidth = maxContentWidth + 4
        val boxHeight = contentLines.length + 2
        val entryLines = countEntryLines(entries.asScala.toList)
        val scrollBottom = rows - entryLines

        val anchorRow = bannerPos.vertical match {
          case VerticalPosition.Top => 1
          case VerticalPosition.Center => math.max(1, (scrollBottom - boxHeight) / 2 + 1)
          case VerticalPosition.Bottom => math.max(1, scrollBottom - boxHeight + 1)
        }
        val anchorCol = bannerPos.horizontal match {
          case HorizontalPosition.Left => 1
          case HorizontalPosition.Center => math.max(1, (cols - boxWidth) / 2 + 1)
          case HorizontalPosition.Right => math.max(1, cols - boxWidth + 1)
        }
        val startRow = math.max(1, math.min(scrollBottom - boxHeight + 1, anchorRow + bannerPos.rowOffset))
        val startCol = math.max(1, math.min(cols - boxWidth + 1, anchorCol + bannerPos.colOffset))

        val borderColor = ANSI.fg.Cyan
        sb.append(ANSI.ctrl.Reset)

        sb.append(ANSI.ctrl.CursorMove(startRow, startCol))
        sb.append(borderColor(s"╭${"─" * (boxWidth - 2)}╮"))
        for (i <- contentLines.indices) {
          val padding = maxContentWidth - plainLines(i).length
          sb.append(ANSI.ctrl.CursorMove(startRow + 1 + i, startCol))
          sb.append(s"${borderColor("│")} ${contentLines(i)}${" " * padding} ${borderColor("│")}")
        }
        sb.append(ANSI.ctrl.CursorMove(startRow + boxHeight - 1, startCol))
        sb.append(borderColor(s"╰${"─" * (boxWidth - 2)}╯"))
        sb.append(ANSI.ctrl.Reset)
    }
  }

  // ── Dispose ────────────────────────────────────────────────────────────

  override def dispose(): Unit = lock.synchronized {
    if (disposed) return
    disposed = true

    val (rows, cols) = terminal.size()
    val entryList = entries.asScala.toList
    val sb = new StringBuilder(512)

    // Erase banner before resetting scroll region
    if (bannerContent.isDefined) {
      eraseBannerAbsolute(sb, rows, cols, linesOnScreen)
    }

    if (scrollRegionActive) {
      sb.append(ANSI.ctrl.ResetScrollRegion)
      scrollRegionActive = false
    }

    bannerContent = None

    // Erase entry area
    if (linesOnScreen > 0) {
      val entryLines = countEntryLines(entryList)
      val startRow = rows - math.max(linesOnScreen, entryLines) + 1
      sb.append(ANSI.ctrl.Reset)
      for (row <- startRow to rows) {
        sb.append(ANSI.ctrl.CursorMove(row, 1))
        sb.append(ANSI.ctrl.EraseLineEnd)
      }
      sb.append(ANSI.ctrl.CursorMove(startRow, 1))
    } else {
      appendEraseRelative(sb, linesOnScreen)
    }
    linesOnScreen = 0

    entryList.foreach { e =>
      if (!e.isCompleted) {
        val content = e.currentContent
        if (content.nonEmpty) { sb.append(content); sb.append("\n") }
      }
    }
    entries.clear()
    sb.append(ANSI.ctrl.ShowCursor)
    terminal.write(sb.toString())

    System.setOut(savedOut)
    System.setErr(savedErr)
    Logger.root.copy(handlers = originalHandlers).replace()
    Overlay.clear()
  }

  // ── Utilities ──────────────────────────────────────────────────────────

  private def countEntryLines(entryList: List[OverlayEntry]): Int =
    entryList.map(_.currentContent.count(_ == '\n') + 1).sum

  private def truncateLines(text: String, cols: Int): String =
    text.split("\n", -1).map(truncateAnsi(_, cols)).mkString("\n")

  private def truncateAnsi(s: String, maxCols: Int): String = {
    val sb = new StringBuilder(s.length)
    var visibleCount = 0
    var i = 0
    while (i < s.length && visibleCount < maxCols) {
      if (s.charAt(i) == '\u001b' && i + 1 < s.length && s.charAt(i + 1) == '[') {
        sb.append('\u001b'); sb.append('['); i += 2
        while (i < s.length && !s.charAt(i).isLetter) { sb.append(s.charAt(i)); i += 1 }
        if (i < s.length) { sb.append(s.charAt(i)); i += 1 }
      } else { sb.append(s.charAt(i)); visibleCount += 1; i += 1 }
    }
    if (visibleCount >= maxCols && i < s.length) sb.append(ANSI.ctrl.Reset)
    sb.toString()
  }

  private def stripAnsi(s: String): String = s.replaceAll("\u001b\\[[0-9;]*[a-zA-Z]", "")
}

private class NoOpOverlay extends Overlay {
  override def entry(initialText: String): OverlayEntry = NoOpOverlay.noOpEntry
  override def progressBar(label: String, width: Int): OverlayEntry = NoOpOverlay.noOpEntry
  override def banner(content: String, position: BannerPosition): Unit = ()
  override def clearBanner(): Unit = ()
  override def size: Int = 0
  override def dispose(): Unit = Overlay.clear()
}

private object NoOpOverlay {
  val noOpEntry: OverlayEntry = new OverlayEntry(null, 40) {
    override def update(text: String): Unit = ()
    override def update(percent: Int, label: String): Unit = ()
    override def complete(message: String): Unit = ()
    override def remove(): Unit = ()
    override def isCompleted: Boolean = true
  }
}
