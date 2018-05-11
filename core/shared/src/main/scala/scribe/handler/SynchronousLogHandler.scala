package scribe.handler

import scribe.LogRecord
import scribe.format.Formatter
import scribe.modify.LogModifier
import scribe.writer.{ConsoleWriter, Writer}

case class SynchronousLogHandler(formatter: Formatter = Formatter.default,
                                 writer: Writer = ConsoleWriter,
                                 modifiers: List[LogModifier] = Nil) extends LogHandler {
  def withFormatter(formatter: Formatter): LogHandler = copy(formatter = formatter)

  def withWriter(writer: Writer): LogHandler = copy(writer = writer)

  override def setModifiers(modifiers: List[LogModifier]): LogHandler = copy(modifiers = modifiers)

  override def log[M](record: LogRecord[M]): Unit = synchronized {
    modifiers.foldLeft(Option(record))((r, lm) => r.flatMap(lm.apply)).foreach { r =>
      writer.write(record, formatter.format(r))
    }
  }
}