package scribe.filter

import scribe.LogRecord

trait Filter {
  def matches[M](record: LogRecord[M]): Boolean
}