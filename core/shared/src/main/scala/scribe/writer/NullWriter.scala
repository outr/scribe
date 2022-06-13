package scribe.writer

import scribe.LogRecord
import scribe.output.LogOutput
import scribe.output.format.OutputFormat

object NullWriter extends Writer {
  override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = {}
}
