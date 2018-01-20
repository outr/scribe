package scribe2

trait LogModifier {
  def apply(record: LogRecord): Option[LogRecord]
}