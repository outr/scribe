package specs

import scribe.handler.LogHandler
import scribe.modify.LogModifier
import scribe.{LogRecord, Priority}

import scala.collection.mutable.ListBuffer

class TestingHandler() extends LogHandler {
  val records: ListBuffer[LogRecord[_]] = ListBuffer.empty[LogRecord[_]]

  override def log[M](record: LogRecord[M]): Unit = records += record

  def clear(): Unit = records.clear()
}