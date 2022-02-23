package scribe.filter

import scribe.LogRecord

/**
  * Filter for use in FilterBuilder, which is a LogModifier
  */
trait Filter {
  def matches[M](record: LogRecord[M]): Boolean

  def &&(that: Filter): Filter = MultiFilter(List(this, that))
}