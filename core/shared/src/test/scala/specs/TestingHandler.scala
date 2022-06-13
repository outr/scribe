package specs

import scribe.LogRecord
import scribe.handler.LogHandler

import scala.collection.mutable.ListBuffer

class TestingHandler() extends LogHandler {
  val records: ListBuffer[LogRecord] = ListBuffer.empty[LogRecord]

  override def log(record: LogRecord): Unit = records += record

  def clear(): Unit = records.clear()
}