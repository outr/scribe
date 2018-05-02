package scribe

import scribe.modify.{LevelFilter, LogModifier}

trait LogSupport[L <: LogSupport[L]] {
  def modifiers: List[LogModifier]

  def setModifiers(modifiers: List[LogModifier]): L

  def clearModifiers(): L = setModifiers(Nil)

  final def withModifier(modifier: LogModifier): L = setModifiers((modifiers.filterNot(_.id == modifier.id) ::: List(modifier)).sorted)
  final def withoutModifier(modifier: LogModifier): L = setModifiers(modifiers.filterNot(_ eq modifier))

  def withMinimumLevel(level: Level): L = withModifier(LevelFilter >= level)

  def includes(level: Level): Boolean = {
    modifiers.find(_.id == LevelFilter.Id).map(_.asInstanceOf[LevelFilter]).forall(_.accepts(level.value))
  }

  def log[M](record: LogRecord[M]): Unit
}