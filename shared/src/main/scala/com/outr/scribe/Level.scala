package com.outr.scribe

case class Level(name: String, value: Double) {
  Level.maxLength = math.max(Level.maxLength, name.length)

  def namePaddedRight: String = name.padTo(Level.maxLength, " ").mkString
}

object Level {
  private var maxLength = 0

  val Trace: Level = Level("TRACE", 100.0)
  val Debug: Level = Level("DEBUG", 200.0)
  val Info: Level = Level("INFO", 300.0)
  val Warn: Level = Level("WARN", 400.0)
  val Error: Level = Level("ERROR", 500.0)
}