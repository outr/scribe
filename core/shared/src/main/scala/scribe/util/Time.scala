package scribe.util

object Time {
  var function: () => Long = _

  reset()

  def apply(): Long = function()

  def reset(): Unit = function = () => System.currentTimeMillis()
}