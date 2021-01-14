package scribe.filter

import scribe.LogRecord

/**
  * Filter matcher based on the class name
  */
object ClassNameFilter extends FilterMatcher {
  override protected def string[M](record: LogRecord[M]): String = record.className
}