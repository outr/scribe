package scribe.writer

import scribe.output.LogOutput
import scribe.output.format.OutputFormat
import scribe.{LogRecord, Logger}

/**
 * SystemErrWriter writes logs to System.err
 */
object SystemErrWriter extends Writer {
  override def write[M](record: LogRecord[M], output: LogOutput, outputFormat: OutputFormat): Unit =
    SystemWriter.write(Logger.system.err, output, outputFormat)
}