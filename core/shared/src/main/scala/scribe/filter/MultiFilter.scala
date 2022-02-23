package scribe.filter

import scribe.LogRecord

case class MultiFilter(filters: List[Filter]) extends Filter {
  override def matches[M](record: LogRecord[M]): Boolean = filters.forall(_.matches(record))

  override def &&(that: Filter): Filter = copy(filters ::: List(that))
}