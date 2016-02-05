package com.outr.scribe.writer

import com.outr.scribe.{Logger, LogRecord}
import com.outr.scribe.formatter.Formatter

object ErrorWriter extends Writer {
   def write(record: LogRecord, formatter: Formatter): Unit = {
     Logger.systemErr.print(formatter.format(record))
   }
 }
