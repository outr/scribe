package scribe

import scribe.modify.LogModifier

trait LogSupport[L <: LogSupport[L]] {
  def withModifier(modifier: LogModifier): L
  def withoutModifier(modifier: LogModifier): L

  // TODO: withFilter, withLevelFilter
}