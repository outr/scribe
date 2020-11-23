package scribe.writer

import scribe.LogRecord
import scribe.output.LogOutput
import scribe.output.format.OutputFormat

trait Writer {
  def write[M](record: LogRecord[M], output: LogOutput, outputFormat: OutputFormat): Unit

  def dispose(): Unit = {}
}