package com.outr.scribe.writer

import com.outr.scribe.LogRecord
import com.outr.scribe.formatter.Formatter

trait Writer {
  def write(record: LogRecord, formatter: Formatter): Unit
}
