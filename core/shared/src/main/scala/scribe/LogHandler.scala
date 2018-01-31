package scribe

import scribe.format.Formatter
import scribe.modify.{LevelFilter, LogModifier}
import scribe.writer.{ConsoleWriter, Writer}

trait LogHandler extends LogSupport[LogHandler] {
  def formatter: Formatter
  def writer: Writer

  def withFormatter(formatter: Formatter): LogHandler
  def withWriter(writer: Writer): LogHandler
  def update(formatter: Formatter = formatter,
             writer: Writer = writer,
             modifiers: List[LogModifier] = modifiers): LogHandler = {
    withFormatter(formatter).withWriter(writer).setModifiers(modifiers)
  }
}

case class SynchronousLogHandler(formatter: Formatter = Formatter.default,
                                 writer: Writer = ConsoleWriter,
                                 modifiers: List[LogModifier] = Nil) extends LogHandler {
  override def withFormatter(formatter: Formatter): LogHandler = copy(formatter = formatter)
  override def withWriter(writer: Writer): LogHandler = copy(writer = writer)

  override def setModifiers(modifiers: List[LogModifier]): LogHandler = copy(modifiers = modifiers)

  override def log(record: LogRecord): Unit = {
    modifiers.foldLeft(Option(record))((r, lm) => r.flatMap(lm.apply)).foreach { r =>
      writer.write(formatter.format(r))
    }
  }
}

object LogHandler {
  lazy val default: LogHandler = SynchronousLogHandler()

  def apply(formatter: Formatter = Formatter.default,
            writer: Writer = ConsoleWriter,
            minimumLevel: Level = Level.Info,
            modifiers: List[LogModifier] = Nil): LogHandler = {
    SynchronousLogHandler(formatter, writer, List(LevelFilter >= minimumLevel) ::: modifiers)
  }
}