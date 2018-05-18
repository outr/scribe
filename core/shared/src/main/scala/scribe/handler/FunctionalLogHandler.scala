package scribe.handler

import scribe.LogRecord
import scribe.format.Formatter
import scribe.modify.LogModifier
import scribe.writer.{ConsoleWriter, Writer}

case class FunctionalLogHandler(f: LogRecord[_] => Unit, override val modifiers: List[LogModifier]) extends LogHandler {
  override def formatter: Formatter = Formatter.default
  override def writer: Writer = ConsoleWriter

  override def withFormatter(formatter: Formatter): LogHandler = this

  override def withWriter(writer: Writer): LogHandler = this

  override def setModifiers(modifiers: List[LogModifier]): LogHandler = copy(modifiers = modifiers)

  override def log[M](record: LogRecord[M]): Unit = {
    modifiers.foldLeft(Option(record))((r, lm) => r.flatMap(lm.apply)).foreach { r =>
      f(r)
    }
  }
}
