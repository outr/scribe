package scribe.modify

import scribe.{LogRecord, Priority}

case class LogBooster(booster: Double => Double,
                      priority: Priority,
                      id: String = "") extends LogModifier {
  override def apply[M](record: LogRecord[M]): Option[LogRecord[M]] = Some(record.boost(booster))

  override def withId(id: String): LogModifier = copy(id = id)
}

object LogBooster {
  def multiply(multiplier: Double, priority: Priority = Priority.Normal): LogBooster = new LogBooster(_ * multiplier, priority)
  def add(value: Double, priority: Priority = Priority.Normal): LogBooster = new LogBooster(_ + value, priority)
  def subtract(value: Double, priority: Priority = Priority.Normal): LogBooster = new LogBooster(_ - value, priority)
}