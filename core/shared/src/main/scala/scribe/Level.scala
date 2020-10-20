package scribe

case class Level(name: String, value: Double) {
  def namePadded: String = Level.padded(this)

  Level.add(this)
}

object Level {
  private var maxLength = 0

  private var map = Map.empty[String, Level]
  private var padded = Map.empty[Level, String]

  implicit final val LevelOrdering: Ordering[Level] = Ordering.by[Level, Double](_.value).reverse

  val Trace: Level = Level("TRACE", 100.0)
  val Debug: Level = Level("DEBUG", 200.0)
  val Info: Level = Level("INFO", 300.0)
  val Warn: Level = Level("WARN", 400.0)
  val Error: Level = Level("ERROR", 500.0)
  val Fatal: Level = Level("FATAL", 600.0)

  def add(level: Level): Unit = synchronized {
    val length = level.name.length
    map += level.name.toLowerCase -> level
    if (length > maxLength) {
      maxLength = length
      padded = map.map {
        case (_, level) => level -> level.name.padTo(maxLength, " ").mkString
      }
    } else {
      padded += level -> level.name.padTo(maxLength, " ").mkString
    }
  }

  def get(name: String): Option[Level] = map.get(name.toLowerCase)

  def apply(name: String): Level = get(name).getOrElse(throw new RuntimeException(s"Level not found by name: $name"))
}