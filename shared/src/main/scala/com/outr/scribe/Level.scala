package com.outr.scribe

class Level(val name: String, val value: Double) {
  Level.maxLength = math.max(Level.maxLength, name.length)

  def namePaddedRight: String = name.padTo(Level.maxLength, " ").mkString
}

object Level {
  private var maxLength = 0

  def apply(name: String, value: Double): Level = new Level(name, value)

  case object Trace extends Level("TRACE", 100.0)
  case object Debug extends Level("DEBUG", 200.0)
  case object Info extends Level("INFO", 300.0)
  case object Warn extends Level("WARN", 400.0)
  case object Error extends Level("ERROR", 500.0)
}