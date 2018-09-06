package scribe.writer

import scribe._
import scribe.Platform._

object BrowserConsoleWriter extends Writer {
  override def write[M](record: LogRecord[M], output: String): Unit = {
    if (record.level >= Level.Error) {
      console.error(output)
    } else if (record.level >= Level.Warn) {
      console.warn(output)
    } else {
      console.log(output)
    }
  }
}