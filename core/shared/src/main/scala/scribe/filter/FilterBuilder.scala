package scribe.filter

import scribe.modify.LogModifier
import scribe.{Level, LogRecord, Priority}

case class FilterBuilder(priority: Priority = Priority.Normal,
                         select: List[Filter] = Nil,
                         include: List[Filter] = Nil,
                         exclude: List[Filter] = Nil,
                         booster: Double => Double = d => d,
                         _excludeUnselected: Boolean = false) extends LogModifier {
  def select(filters: Filter*): FilterBuilder = copy(select = select ::: filters.toList)
  def include(filters: Filter*): FilterBuilder = copy(include = include ::: filters.toList)
  def exclude(filters: Filter*): FilterBuilder = copy(exclude = exclude ::: filters.toList)

  def excludeUnselected: FilterBuilder = copy(_excludeUnselected = true)
  def includeUnselected: FilterBuilder = copy(_excludeUnselected = false)

  def boost(booster: Double => Double): FilterBuilder = copy(booster = booster)
  def setLevel(level: Level): FilterBuilder = boost(_ => level.value)
  def boostOneLevel: FilterBuilder = boost(d => d + 100.0)
  def boosted(minimumLevel: Level,
              destinationLevel: Level): FilterBuilder = {
    boost(d => if (d >= minimumLevel.value && d <= destinationLevel.value) {
      destinationLevel.value
    } else {
      d
    })
  }

  def priority(priority: Priority): FilterBuilder = copy(priority = priority)

  override def apply[M](record: LogRecord[M]): Option[LogRecord[M]] = {
    if (select.exists(_.matches(record))) {
      val incl = include.forall(_.matches(record))
      val excl = exclude.exists(_.matches(record))
      if (incl && !excl) {
        Some(record.boost(booster))
      } else {
        None
      }
    } else if (_excludeUnselected) {
      None
    } else {
      Some(record)
    }
  }
}