package scribe.modify

import scribe.LogRecord

trait LogModifier {
  def apply(record: LogRecord): Option[LogRecord]
}