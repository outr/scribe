package scribe.modify

import scribe.{Level, LogRecord, Priority}

class LevelFilter(include: Double => Boolean,
                  exclude: Double => Boolean,
                  override val priority: Priority) extends LogModifier {
  override def id: String = LevelFilter.Id

  def accepts(level: Double): Boolean = include(level) && !exclude(level)

  override def apply[M](record: LogRecord[M]): Option[LogRecord[M]] = if (accepts(record.value)) {
    Some(record)
  } else {
    None
  }
}

object LevelFilter {
  val Id: String = "LevelFilter"

  def >=(level: Level): LevelFilter = new LevelFilter(level <= _, _ => false, Priority.High)
}