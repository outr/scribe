package scribe.filter

import scribe.LogRecord

/**
  * Filters based on the package name
  */
object PackageNameFilter extends FilterMatcher {
  override protected def string(record: LogRecord): String = {
    val index = record.className.lastIndexOf('.')
    if (index > 0) {
      record.className.substring(0, index)
    } else {
      record.className
    }
  }
}