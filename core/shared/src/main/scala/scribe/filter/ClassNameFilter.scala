package scribe.filter

import scribe.LogRecord

object ClassNameFilter extends FilterMatcher {
  override protected def string[M](record: LogRecord[M]): String = record.className
}