package scribe.overlay

import java.io.{OutputStream, PrintStream}

/**
 * A PrintStream replacement for System.out/err that routes output through
 * the overlay's scroll-region-aware write path.
 */
class OverlayStream private[overlay](overlay: ActiveOverlay)
  extends PrintStream(new OverlayOutputStream(overlay), true) {
  override def toString: String = "Overlay Printer"
}

private[overlay] class OverlayOutputStream(overlay: ActiveOverlay) extends OutputStream {
  private val buffer = new ThreadLocal[StringBuilder] {
    override def initialValue(): StringBuilder = new StringBuilder(256)
  }

  override def write(b: Int): Unit = {
    val c = b.toChar
    val sb = buffer.get()
    if (c == '\n') {
      sb.append('\n')
      overlay.writeToScrollRegion(sb.toString())
      sb.clear()
    } else {
      sb.append(c)
    }
  }

  override def write(b: Array[Byte], off: Int, len: Int): Unit = {
    val s = new String(b, off, len)
    val sb = buffer.get()
    var i = 0
    while (i < s.length) {
      val c = s.charAt(i)
      sb.append(c)
      if (c == '\n') {
        overlay.writeToScrollRegion(sb.toString())
        sb.clear()
      }
      i += 1
    }
  }

  override def flush(): Unit = {
    val sb = buffer.get()
    if (sb.nonEmpty) {
      overlay.writeToScrollRegion(sb.toString())
      sb.clear()
    }
  }
}
