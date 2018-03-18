package specs

import scribe.modify.LogModifier
import scribe.{LogRecord, Priority}

import scala.collection.mutable.ListBuffer

class TestingModifier extends LogModifier {
  override def id: String = "TestingModifier"

  override def priority: Priority = Priority.Normal

  val records: ListBuffer[LogRecord[_]] = ListBuffer.empty[LogRecord[_]]

  override def apply[M](record: LogRecord[M]): Option[LogRecord[M]] = {
    records += record
    Some(record)
  }

  def clear(): Unit = records.clear()
}