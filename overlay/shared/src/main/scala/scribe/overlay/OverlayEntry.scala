package scribe.overlay

import scribe.ANSI

import java.util.concurrent.atomic.AtomicReference

/**
 * A single persistent line in the overlay area at the bottom of the terminal.
 *
 * Entries can be updated in-place from any thread. When completed, their final
 * content is promoted to the log scroll region and the entry is removed.
 */
class OverlayEntry private[overlay](overlay: ActiveOverlay,
                                    private val progressWidth: Int) {
  private val content = new AtomicReference[String]("")
  private var completed = false

  /** Update the entry with arbitrary text. */
  def update(text: String): Unit = {
    content.set(text)
    overlay.redraw()
  }

  /** Update the entry as a progress bar. */
  def update(percent: Int, label: String = ""): Unit = {
    val lbl = if (label.nonEmpty) Some(label) else None
    content.set(ProgressBar.render(width = progressWidth, percent = percent, label = lbl))
    overlay.redraw()
  }

  /** Complete this entry, promoting its final content to the log area. */
  def complete(message: String = ""): Unit = if (!completed) {
    completed = true
    if (message.nonEmpty) {
      content.set(message)
    }
    val finalContent = content.get()
    if (finalContent.nonEmpty) {
      overlay.writeToScrollRegion(finalContent + "\n")
    }
    overlay.removeEntry(this)
  }

  /** Remove this entry without promoting it to the log area. */
  def remove(): Unit = if (!completed) {
    completed = true
    overlay.removeEntry(this)
  }

  /** Whether this entry has been completed or removed. */
  def isCompleted: Boolean = completed

  /** The current rendered content of this entry. */
  def currentContent: String = content.get()
}
