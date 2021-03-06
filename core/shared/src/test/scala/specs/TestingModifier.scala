package specs

import scribe.modify.LogModifier
import scribe.{LogRecord, Priority}

import scala.collection.mutable.ListBuffer

case class TestingModifier(id: String = "TestingModifier") extends LogModifier {
  override def priority: Priority = Priority.Normal

  val records: ListBuffer[LogRecord[_]] = ListBuffer.empty[LogRecord[_]]

  override def apply[M](record: LogRecord[M]): Option[LogRecord[M]] = {
    records += record
    Some(record)
  }

  override def withId(id: String): LogModifier = copy(id = id)

  def clear(): Unit = records.clear()
}