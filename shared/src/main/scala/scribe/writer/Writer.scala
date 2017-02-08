package scribe.writer

import scribe.LogRecord
import scribe.formatter.Formatter

trait Writer {
  def write(record: LogRecord, formatter: Formatter): Unit
}
