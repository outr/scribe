package scribe

import scribe.modify.{LevelFilter, LogModifier}

trait LogSupport[L <: LogSupport[L]] {
  def modifiers: List[LogModifier]

  def setModifiers(modifiers: List[LogModifier]): L

  def clearModifiers(): L = setModifiers(Nil)

  final def withModifier(modifier: LogModifier): L = setModifiers((modifiers ::: List(modifier)).sorted)
  final def withoutModifier(modifier: LogModifier): L = setModifiers(modifiers.filterNot(_ eq modifier))

  def withMinimumLevel(level: Level): L = withModifier(LevelFilter >= level)

  def log[M](record: LogRecord[M]): Unit
}