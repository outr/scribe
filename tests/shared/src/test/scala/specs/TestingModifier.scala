package specs

import scribe.LogRecord
import scribe.modify.LogModifier

import scala.collection.mutable.ListBuffer

class TestingModifier extends LogModifier {
  val records: ListBuffer[LogRecord] = ListBuffer.empty[LogRecord]

  override def apply(record: LogRecord): Option[LogRecord] = {
    records += record
    Some(record)
  }

  def clear(): Unit = records.clear()
}