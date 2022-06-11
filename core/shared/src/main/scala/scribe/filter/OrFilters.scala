package scribe.filter

import scribe.LogRecord

case class OrFilters(filters: List[Filter]) extends Filter {
  override def matches(record: LogRecord): Boolean = filters.exists(_.matches(record))

  override def ||(that: Filter): Filter = copy(filters ::: List(that))
}