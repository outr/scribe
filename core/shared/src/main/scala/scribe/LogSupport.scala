package scribe

import scribe.modify.LogModifier

trait LogSupport {
  type Self <: LogSupport

  def withModifier(modifier: LogModifier): Self
  def withoutModifier(modifier: LogModifier): Self

  def withModifiers(modifiers: LogModifier*): Self = {
    val tail = modifiers.tail
    if (tail.isEmpty) {
      withModifier(modifiers.head)
    } else {
      withModifier(modifiers.head).withModifiers(tail: _*)
    }
  }

  // TODO: withFilter, withLevelFilter
}