package scribe.writer

import scribe._
import scribe.output._

object ConsoleWriter extends Writer {
  /**
    * If true, will always write out plain text. If false, will detect platform and output support for rich output. This
    * presumes that the `Formatter` is not already creating plain text. Defaults to false.
    */
  var OnlyPlainText: Boolean = false

  /**
    * If true, will always synchronize writing to the console to avoid interleaved text. Most native consoles will
    * handle this automatically, but IntelliJ and Eclipse are notorious about not properly handling this.
    * Defaults to true.
    */
  var SynchronizeWriting: Boolean = true

  override def write[M](record: LogRecord[M], output: LogOutput): Unit = {
    Platform.consoleWriter.write[M](record, output)
  }
}