package scribe.filter

import scribe.LogRecord

object PackageNameFilter extends FilterMatcher {
  override protected def string[M](record: LogRecord[M]): String = {
    val index = record.className.lastIndexOf('.')
    if (index > 0) {
      record.className.substring(0, index)
    } else {
      record.className
    }
  }
}