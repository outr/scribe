package scribe.filter

import scribe.modify.LogModifier
import scribe.{LogRecord, Priority}

case class FilterBuilder(priority: Priority = Priority.Normal,
                         select: List[Filter] = Nil,
                         include: List[Filter] = Nil,
                         exclude: List[Filter] = Nil) extends LogModifier {
  def select(filters: Filter*): FilterBuilder = copy(select = select ::: filters.toList)
  def include(filters: Filter*): FilterBuilder = copy(include = include ::: filters.toList)
  def exclude(filters: Filter*): FilterBuilder = copy(exclude = exclude ::: filters.toList)

  def priority(priority: Priority): FilterBuilder = copy(priority = priority)

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
      Some(record)
    }
  }
}