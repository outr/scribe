package scribe.writer

import scribe.{Logger, LogRecord}
import scribe.format.Formatter

object ErrorWriter extends Writer {
   def write(record: LogRecord, formatter: Formatter): Unit = {
     Logger.systemErr.print(formatter.format(record))
   }
 }
