package scribe.util

object Time {
  var function: () => Long = _

  reset()

  def apply(): Long = function()

  def contextualize[Return](t: => Long)(f: => Return): Return = {
    val old = function
    function = () => t
    try {
      f
    } finally {
      function = old
    }
  }

  def reset(): Unit = function = () => System.currentTimeMillis()
}
