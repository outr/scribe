package scribe.filter

import scribe.LogRecord

/**
  * Filter matcher based on the class name
  */
object ClassNameFilter extends FilterMatcher {
  override protected def string(record: LogRecord): String = record.className
}