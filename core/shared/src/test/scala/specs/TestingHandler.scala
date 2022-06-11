package specs

import scribe.handler.LogHandler
import scribe.modify.LogModifier
import scribe.{LogRecord, Priority}

import scala.collection.mutable.ListBuffer

class TestingHandler() extends LogHandler {
  val records: ListBuffer[LogRecord] = ListBuffer.empty[LogRecord]

  override def log(record: LogRecord): Unit = records += record

  def clear(): Unit = records.clear()
}