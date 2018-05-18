package scribe.handler

import scribe.LogRecord
import scribe.format.Formatter
import scribe.modify.LogModifier
import scribe.writer.{ConsoleWriter, Writer}

case class SynchronousLogHandler(formatter: Formatter = Formatter.default,
                                 writer: Writer = ConsoleWriter,
                                 modifiers: List[LogModifier] = Nil) extends LogHandler {
  override def withFormatter(formatter: Formatter): LogHandler = copy(formatter = formatter)

  override def withWriter(writer: Writer): LogHandler = copy(writer = writer)

  override def setModifiers(modifiers: List[LogModifier]): LogHandler = copy(modifiers = modifiers)

  override def log[M](record: LogRecord[M]): Unit = synchronized {
    SynchronousLogHandler.log(this, record)
  }
}

object SynchronousLogHandler {
  def log[M](handler: LogHandler, record: LogRecord[M]): Unit = {
    handler.modifiers.foldLeft(Option(record))((r, lm) => r.flatMap(lm.apply)).foreach { r =>
      handler.writer.write(record, handler.formatter.format(r))
    }
  }
}