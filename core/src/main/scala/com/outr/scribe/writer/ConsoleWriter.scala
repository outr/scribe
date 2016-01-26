package com.outr.scribe.writer

import com.outr.scribe.{Logger, LogRecord}
import com.outr.scribe.formatter.Formatter

object ConsoleWriter extends Writer {
  def write(record: LogRecord, formatter: Formatter): Unit = {
    Logger.systemOut.print(formatter.format(record))
  }
}
