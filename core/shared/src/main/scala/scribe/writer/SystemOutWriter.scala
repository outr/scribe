package scribe.writer

import scribe.output.LogOutput
import scribe.output.format.OutputFormat
import scribe.{LogRecord, Logger}

/**
 * SystemOutWriter writes logs to System.out
 */
object SystemOutWriter extends Writer {
  override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit =
    SystemWriter.write(Logger.system.out, output, outputFormat)
}