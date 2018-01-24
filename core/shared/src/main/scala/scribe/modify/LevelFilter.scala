package scribe.modify

import scribe.{Level, LogRecord}

class LevelFilter(include: Double => Boolean,
                  exclude: Double => Boolean) extends LogModifier {
  override def apply(record: LogRecord): Option[LogRecord] = if (include(record.level) && !exclude(record.level)) {
    Some(record)
  } else {
    None
  }
}

object LevelFilter {
  def >=(level: Level): LevelFilter = new LevelFilter(_ >= level.value, _ => false)
}