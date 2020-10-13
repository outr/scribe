package scribe

class Priority(val value: Double) extends AnyVal with Ordered[Priority] {
  def +(value: Double): Priority = new Priority(value + value)
  def -(value: Double): Priority = new Priority(value - value)

  override def compare(that: Priority): Int = -value.compare(that.value)
}

object Priority {
  lazy val Highest: Priority = new Priority(Double.MaxValue)
  lazy val Critical: Priority = new Priority(1000.0)
  lazy val Important: Priority = new Priority(100.0)
  lazy val High: Priority = new Priority(10.0)
  lazy val Normal: Priority = new Priority(0.0)
  lazy val Low: Priority = new Priority(-10.0)
  lazy val Lower: Priority = new Priority(-100.0)
  lazy val Fallthrough: Priority = new Priority(-1000.0)
  lazy val Lowest: Priority = new Priority(Double.MinValue)
}