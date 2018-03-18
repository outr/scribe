package scribe.modify

import scribe.{LogRecord, Priority}

trait LogModifier extends Ordered[LogModifier] {
  /**
    * Represents a unique identifier for this type of modifier. This is used when adding a LogModifier to a Logger to
    * replace by type.
    */
  def id: String = getClass.getName
  def priority: Priority
  def apply[M](record: LogRecord[M]): Option[LogRecord[M]]

  override def compare(that: LogModifier): Int = that.priority.compare(priority)
}