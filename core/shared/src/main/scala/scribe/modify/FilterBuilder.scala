package scribe.modify

import scribe.{Level, LogRecord, Priority}

case class FilterBuilder(priority: Priority = Priority.Normal,
                         matchers: List[Filter] = Nil) extends LogModifier {
  def withFilter(matchers: Filter*): FilterBuilder = copy(matchers = this.matchers ::: matchers.toList)
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
  }

  override def apply[M](record: LogRecord[M]): Option[LogRecord[M]] = {
    val include = `type` match {
      case FilterType.Inclusion => matchers.forall(_.matches(record))
      case FilterType.Exclusion => matchers.collectFirst {
        case f if f.matches(record) => false
      }.getOrElse(true)
    }
    if (include) {
      Some(record)
    } else {
      None
    }
  }
}

object FilterBuilder extends FilterBuilder()

trait Filter {
  def matches[M](record: LogRecord[M]): Boolean
}

sealed trait FilterType

object FilterType {
  case object Include extends FilterType
  case object Exclude extends FilterType
}