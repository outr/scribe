package scribe.util

object Time {
  var function: () => Long = () => System.currentTimeMillis()

  def apply(): Long = function()
}