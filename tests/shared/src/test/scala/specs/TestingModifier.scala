package specs

import scribe.modify.LogModifier
import scribe.{LogRecord, Priority}

import scala.collection.mutable.ListBuffer

class TestingModifier extends LogModifier {
  override def priority: Priority = Priority.Normal

  val records: ListBuffer[LogRecord] = ListBuffer.empty[LogRecord]

  override def apply(record: LogRecord): Option[LogRecord] = {
    records += record
    Some(record)
  }

  def clear(): Unit = records.clear()
}