package scribe.modify

import scribe.{LogRecord, Priority}

class ClassNameFilter(includeClassName: String => Boolean, exclude: Boolean, val priority: Priority) extends LogModifier {
  override def apply[M](record: LogRecord[M]): Option[LogRecord[M]] = {
    val include = includeClassName(record.className)
    if ((exclude && !include) || (!exclude && include)) {
      Some(record)
    } else {
      None
    }
  }
}

object ClassNameFilter {
  def apply(className: String, exclude: Boolean, priority: Priority = Priority.Normal): ClassNameFilter = {
    new ClassNameFilter(_ == className, exclude, priority)
  }
  def contains(value: String, exclude: Boolean, priority: Priority = Priority.Normal): ClassNameFilter = {
    new ClassNameFilter(_.contains(value), exclude, priority)
  }
  def startsWith(value: String, exclude: Boolean, priority: Priority = Priority.Normal): ClassNameFilter = {
    new ClassNameFilter(_.startsWith(value), exclude, priority)
  }
  def endsWith(value: String, exclude: Boolean, priority: Priority = Priority.Normal): ClassNameFilter = {
    new ClassNameFilter(_.endsWith(value), exclude, priority)
  }
  def regex(regex: String, exclude: Boolean, priority: Priority = Priority.Normal): ClassNameFilter = {
    new ClassNameFilter(_.matches(regex), exclude, priority)
  }
}