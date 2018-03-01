package scribe.modify

import scribe.{Level, LogRecord, Priority}

class LevelFilter(include: Double => Boolean,
                  exclude: Double => Boolean,
                  override val priority: Priority) extends LogModifier {
  override def apply(record: LogRecord): Option[LogRecord] = if (include(record.value) && !exclude(record.value)) {
    Some(record)
  } else {
    None
  }
}

object LevelFilter {
  def >=(level: Level): LevelFilter = new LevelFilter(level <= _, _ => false, Priority.High)
}