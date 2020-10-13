package scribe

class Level private(val name: String, val value: Double) extends Ordered[Double] {
  private var _namePaddedRight: String = name.padTo(Level.maxLength, " ").mkString

  def namePaddedRight: String = _namePaddedRight

  override def compare(that: Double): Int = this.value.compare(that)
}

object Level {
  private var maxLength = 5

  private var levels: Map[String, Level] = Map.empty

  def apply(name: String, value: Double): Level = synchronized {
    val length = name.length
    if (length > maxLength) {
      maxLength = length
      levels.values.foreach { l =>
        l._namePaddedRight = l.name.padTo(maxLength, " ").mkString
      }
    }
    val level = new Level(name, value)
    levels += level.name -> level
    level
  }

  case object Trace extends Level("TRACE", 100.0)
  case object Debug extends Level("DEBUG", 200.0)
  case object Info extends Level("INFO", 300.0)
  case object Warn extends Level("WARN", 400.0)
  case object Error extends Level("ERROR", 500.0)
  case object Fatal extends Level("FATAL", 600.0)
}