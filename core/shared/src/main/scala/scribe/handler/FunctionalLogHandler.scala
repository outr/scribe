package scribe.handler

import scribe.LogRecord
import scribe.modify.LogModifier

case class FunctionalLogHandler(f: LogRecord => Unit, override val modifiers: List[LogModifier]) extends LogHandler {
  override def setModifiers(modifiers: List[LogModifier]): LogHandler = copy(modifiers = modifiers)

  override def log(record: LogRecord): Unit = {
    modifiers.foldLeft(Option(record))((r, lm) => r.flatMap(lm.apply)).foreach { r =>
      f(r)
    }
  }
}
