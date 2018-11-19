package scribe.writer

import scribe._
import scribe.Platform._
import scribe.output.LogOutput

object BrowserConsoleWriter extends Writer {
  override def write[M](record: LogRecord[M], output: LogOutput): Unit = {
    // TODO: Support stylized output
    if (record.level >= Level.Error) {
      console.error(output.plainText)
    } else if (record.level >= Level.Warn) {
      console.warn(output.plainText)
    } else {
      console.log(output.plainText)
    }
  }
}