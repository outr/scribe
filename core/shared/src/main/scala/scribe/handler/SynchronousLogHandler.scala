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

  def setModifiers(modifiers: List[LogModifier]): LogHandler = copy(modifiers = modifiers.sorted)

  override def log[M](record: LogRecord[M]): Unit = {
    SynchronousLogHandler.log(modifiers, formatter, writer, record)
  }
}

object SynchronousLogHandler {
  def log[M](modifiers: List[LogModifier], formatter: Formatter, writer: Writer, record: LogRecord[M]): Unit = {
    modifiers.foldLeft(Option(record))((r, lm) => r.flatMap(lm.apply)).foreach { r =>
      val logOutput = formatter.format(r)
      writer.write(record, logOutput)
    }
  }
}