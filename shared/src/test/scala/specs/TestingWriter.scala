package specs

import com.outr.scribe.LogRecord
import com.outr.scribe.formatter.Formatter
import com.outr.scribe.writer.Writer

import scala.collection.mutable.ListBuffer

class TestingWriter extends Writer {
  val records: ListBuffer[LogRecord] = ListBuffer.empty[LogRecord]

  def write(record: LogRecord, formatter: Formatter): Unit = records += record

  def clear(): Unit = records.clear()
}