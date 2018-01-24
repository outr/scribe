package scribe.modify

import scribe.LogRecord

class LogBooster(booster: Double => Double) extends LogModifier {
  override def apply(record: LogRecord): Option[LogRecord] = Some(record.boost(booster))
}

object LogBooster {
  def multiply(multiplier: Double): LogBooster = new LogBooster(_ * multiplier)
  def add(value: Double): LogBooster = new LogBooster(_ + value)
  def subtract(value: Double): LogBooster = new LogBooster(_ - value)
}