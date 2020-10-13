package scribe.handler

import scribe.LogRecord
import scribe.modify.LogModifier

case class FunctionalLogHandler(f: LogRecord[_] => Unit, modifiers: List[LogModifier]) extends LogHandler {
  def setModifiers(modifiers: List[LogModifier]): LogHandler = copy(modifiers = modifiers.sorted)

  override def log[M](record: LogRecord[M]): Unit = {
    modifiers.foldLeft(Option(record))((r, lm) => r.flatMap(lm.apply)).foreach { r =>
      f(r)
    }
  }
}