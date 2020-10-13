package scribe.modify

import scribe.filter.Filter
import scribe.{Level, LogRecord, Priority}

class LevelFilter(include: Double => Boolean,
                  exclude: Double => Boolean,
                  override val priority: Priority) extends LogModifier with Filter {
  override def id: String = LevelFilter.Id

  def accepts(level: Double): Boolean = include(level) && !exclude(level)

  override def apply[M](record: LogRecord[M]): Option[LogRecord[M]] = if (accepts(record.value)) {
    Some(record)
  } else {
    None
  }

  override def matches[M](record: LogRecord[M]): Boolean = accepts(record.value)
}

object LevelFilter {
  val Id: String = "LevelFilter"

  lazy val ExcludeAll: LevelFilter = new LevelFilter(include = _ => false, exclude = _ => true, Priority.Low)
  lazy val IncludeAll: LevelFilter = new LevelFilter(include = _ => true, exclude = _ => false, Priority.Low)

  def >(level: Level): LevelFilter = new LevelFilter(
    include = _ > level.value,
    exclude = _ => false,
    priority = Priority.High
  )
  def >=(level: Level): LevelFilter = new LevelFilter(
    include = _ >= level.value,
    exclude = _ => false,
    priority = Priority.High
  )
  def <(level: Level): LevelFilter = new LevelFilter(
    include = _ < level.value,
    exclude = _ => false,
    priority = Priority.High
  )
  def <=(level: Level): LevelFilter = new LevelFilter(
    include = _ <= level.value,
    exclude = _ => false,
    priority = Priority.High
  )
}