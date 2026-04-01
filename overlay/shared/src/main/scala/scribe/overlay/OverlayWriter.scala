package scribe.overlay

import scribe.LogRecord
import scribe.output.LogOutput
import scribe.output.format.OutputFormat
import scribe.writer.Writer

/**
 * A Writer wrapper that routes formatted log output through the overlay's
 * scroll-region-aware write path instead of directly to the terminal.
 */
class OverlayWriter(overlay: ActiveOverlay, val delegate: Writer) extends Writer {
  override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = {
    val sb = new StringBuilder(512)
    outputFormat.begin(sb.append(_))
    outputFormat(output, sb.append(_))
    outputFormat.end(sb.append(_))
    overlay.writeToScrollRegion(sb.toString() + "\n")
  }

  override def dispose(): Unit = delegate.dispose()
}
