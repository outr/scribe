package scribe.modify

import scribe.{LogRecord, Priority}

trait LogModifier extends Ordered[LogModifier] {
  def priority: Priority
  def apply(record: LogRecord): Option[LogRecord]

  override def compare(that: LogModifier): Int = priority.compare(that.priority)
}