package scribe.writer

import scribe.{Logger, LogRecord}
import scribe.formatter.Formatter

object ConsoleWriter extends Writer {
  def write(record: LogRecord, formatter: Formatter): Unit = {
    Logger.systemOut.print(formatter.format(record))
  }
}
