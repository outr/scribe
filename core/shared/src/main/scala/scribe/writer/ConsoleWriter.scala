package scribe.writer

import scribe._
import scribe.output._

object ConsoleWriter extends Writer {
  override def write[M](record: LogRecord[M], output: LogOutput): Unit = synchronized {
    Platform.consoleWriter.write[M](record, output)
  }
}