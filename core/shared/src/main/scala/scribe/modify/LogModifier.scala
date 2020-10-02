package scribe.modify

import scribe.{LogRecord, Priority}

/**
 * LogModifier is attached to `Logger` instances in order to manipulate `LogRecord`s, before they are handled by a
 * `LogHandler`.
 */
trait LogModifier extends Ordered[LogModifier] {
  /**
    * Represents a unique identifier for this type of modifier. This is used when adding a LogModifier to a Logger to
    * replace by type.
    */
  def id: String = getClass.getName

  /**
   * Multiple LogModifiers attached to the same `Logger` are automatically sorted by Priority.
   */
  def priority: Priority

  /**
   * Handles modification of a LogRecord
   *
   * @param record the record to modify
   * @tparam M the type of message
   * @return Some LogRecord that should continue to propagate or None if the logging action should be canceled
   */
  def apply[M](record: LogRecord[M]): Option[LogRecord[M]]

  override def compare(that: LogModifier): Int = that.priority.compare(priority)
}