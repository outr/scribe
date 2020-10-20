package scribe.modify

import scribe.filter.Filter
import scribe.{Level, LogRecord, Priority}

class LevelFilter(include: Double => Boolean,
                  exclude: Double => Boolean,
                  override val priority: Priority,
                  ignoreBoost: Boolean = false) extends LogModifier with Filter {
  override def id: String = LevelFilter.Id

  def accepts(level: Double): Boolean = {
    val i = include(level)
    val e = exclude(level)
    i && !e
  }

  override def apply[M](record: LogRecord[M]): Option[LogRecord[M]] = if (accepts(if (ignoreBoost) record.level.value else record.value)) {
    Some(record)
  } else {
    None
  }

  override def matches[M](record: LogRecord[M]): Boolean = accepts(if (ignoreBoost) record.level.value else record.value)
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