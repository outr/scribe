package scribe.modify

import scribe.{LogRecord, Priority}

trait LogModifier extends Ordered[LogModifier] {
  def priority: Priority
  def apply[M](record: LogRecord[M]): Option[LogRecord[M]]

  override def compare(that: LogModifier): Int = that.priority.compare(priority)
}