package scribe.filter

import scribe.modify.LogModifier
import scribe.{Level, LogRecord, Priority}

case class FilterBuilder(priority: Priority = Priority.Normal,
                         select: List[Filter] = Nil,
                         include: List[Filter] = Nil,
                         exclude: List[Filter] = Nil) extends LogModifier {
  /*def withFilter(matchers: Filter*): FilterBuilder = copy(matchers = this.matchers ::: matchers.toList)
  def filter[M](f: LogRecord[M] => Boolean): FilterBuilder = withFilter(new Filter {
    override def matches[M](record: LogRecord[M]): Boolean = f(record)
  })
  def level(level: Level): FilterBuilder = filter(_.level == level)
  object className {
    def apply(className: String): FilterBuilder = filter(_.className == className)
    def contains(value: String): FilterBuilder = filter(_.className.contains(value))
    def startsWith(value: String): FilterBuilder = filter(_.className.startsWith(value))
    def endsWith(value: String): FilterBuilder = filter(_.className.endsWith(value))
    def regex(regex: String): FilterBuilder = filter(_.className.matches(regex))
  }*/

  def select(filters: Filter*): FilterBuilder = copy(select = select ::: filters.toList)
  def include(filters: Filter*): FilterBuilder = copy(include = include ::: filters.toList)
  def exclude(filters: Filter*): FilterBuilder = copy(exclude = exclude ::: filters.toList)

  override def apply[M](record: LogRecord[M]): Option[LogRecord[M]] = {
    if (select.exists(_.matches(record))) {
      val incl = include.forall(_.matches(record))
      val excl = exclude.exists(_.matches(record))
      if (incl && !excl) {
        Some(record)
      } else {
        None
      }
    } else {
      None
    }
  }
}

trait Filter {
  def matches[M](record: LogRecord[M]): Boolean
}

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

// FilterBuilder.select.packageName.startsWith("org.apache.flink.api").exclude.level(_ < Level.Warn)
// select(packageName.startsWith("org.apache.flink.api").exclude(level < Level.Warn)