package scribe.writer

import scribe.LogRecord
import scribe.output.LogOutput
import scribe.output.format.OutputFormat

object NullWriter extends Writer {
  override def write[M](record: LogRecord[M], output: LogOutput, outputFormat: OutputFormat): Unit = {}
}
