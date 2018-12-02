package scribe.writer

import scribe.LogRecord
import scribe.output.LogOutput

object NullWriter extends Writer {
  override def write[M](record: LogRecord[M], output: LogOutput): Unit = {}
}
