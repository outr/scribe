package scribe.writer

import scribe._
import scribe.output._

object ConsoleWriter extends Writer {
  /**
    * If true, will always write out plain text. If false, will detect platform and output support for rich output. This
    * presumes that the `Formatter` is not already creating plain text. Defaults to false.
    */
  var OnlyPlainText: Boolean = false

  override def write[M](record: LogRecord[M], output: LogOutput): Unit = synchronized {
    Platform.consoleWriter.write[M](record, output)
  }
}