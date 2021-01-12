package scribe.filter

import scribe.LogRecord

/**
  * Matcher for use with filters
  */
trait FilterMatcher {
  protected def string[M](record: LogRecord[M]): String

  def apply(exact: String): Filter = new Filter {
    override def matches[M](record: LogRecord[M]): Boolean = string(record) == exact
  }
  def contains(value: String): Filter = new Filter {
    override def matches[M](record: LogRecord[M]): Boolean = string(record).contains(value)
  }
  def startsWith(value: String): Filter = new Filter {
    override def matches[M](record: LogRecord[M]): Boolean = string(record).startsWith(value)
  }
  def endsWith(value: String): Filter = new Filter {
    override def matches[M](record: LogRecord[M]): Boolean = string(record).endsWith(value)
  }
  def regex(regex: String): Filter = new Filter {
    override def matches[M](record: LogRecord[M]): Boolean = string(record).matches(regex)
  }
}