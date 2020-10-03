package scribe.handler

import scribe.{LogContext, LogRecord}
import scribe.format.Formatter
import scribe.modify.LogModifier
import scribe.writer.{ConsoleWriter, Writer}

case class FunctionalLogHandler(f: LogRecord[_] => Unit, modifiers: List[LogModifier]) extends LogHandler {
  def setModifiers(modifiers: List[LogModifier]): LogHandler = copy(modifiers = modifiers)

  override def log[M](record: LogRecord[M], context: LogContext): Unit = {
    modifiers.foldLeft(Option(record))((r, lm) => r.flatMap(lm.apply)).foreach { r =>
      f(r)
    }
  }
}