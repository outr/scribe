package scribe.modify

import scribe.filter.Filter
import scribe.{Level, LogRecord, Priority}

case class LevelFilter(include: Double => Boolean,
                       exclude: Double => Boolean,
                       priority: Priority,
                       ignoreBoost: Boolean = false,
                       id: String = LevelFilter.Id) extends LogModifier with Filter {
  def accepts(level: Double): Boolean = {
    val i = include(level)
    val e = exclude(level)
    i && !e
  }

  override def apply(record: LogRecord): Option[LogRecord] = if (accepts(if (ignoreBoost) record.level.value else record.levelValue)) {
    Some(record)
  } else {
    None
  }

  override def matches(record: LogRecord): Boolean = accepts(if (ignoreBoost) record.level.value else record.levelValue)

  override def withId(id: String): LogModifier = copy(id = id)
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
  def ===(level: Level): LevelFilter = new LevelFilter(
    include = _ == level.value,
    exclude = _ => false,
    priority = Priority.High
  )
}