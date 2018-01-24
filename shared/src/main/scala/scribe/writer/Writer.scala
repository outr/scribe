package scribe.writer

import scribe.LogRecord
import scribe.format.Formatter

trait Writer {
  def write(record: LogRecord, formatter: Formatter): Unit
}
