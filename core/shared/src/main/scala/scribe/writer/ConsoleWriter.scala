package scribe.writer

import scribe._
import scribe.output._
import scribe.output.format.OutputFormat

object ConsoleWriter extends Writer {
  override def write[M](record: LogRecord[M], output: LogOutput, outputFormat: OutputFormat): Unit = {
    Platform.consoleWriter.write[M](record, output, outputFormat)
  }
}