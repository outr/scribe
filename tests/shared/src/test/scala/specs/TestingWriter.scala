package specs

import scribe.LogRecord
import scribe.formatter.Formatter
import scribe.writer.Writer

import scala.collection.mutable.ListBuffer

class TestingWriter extends Writer {
  val records: ListBuffer[LogRecord] = ListBuffer.empty[LogRecord]

  def write(record: LogRecord, formatter: Formatter): Unit = records += record

  def clear(): Unit = records.clear()
}