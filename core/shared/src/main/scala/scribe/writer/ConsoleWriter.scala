package scribe.writer

import scribe._
import scribe.output._
import scribe.output.format.OutputFormat

object ConsoleWriter extends Writer {
  override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = {
    Platform.consoleWriter.write(record, output, outputFormat)
  }
}