package scribe.handler

import scribe.format.Formatter
import scribe.modify.{LevelFilter, LogModifier}
import scribe.writer.{ConsoleWriter, Writer}
import scribe.{Level, LogRecord, LogSupport}

trait LogHandler extends LogSupport[LogHandler]

object LogHandler {
  lazy val default: LogHandler = SynchronousLogHandler()

  def apply(formatter: Formatter = Formatter.default,
            writer: Writer = ConsoleWriter,
            minimumLevel: Level = Level.Info,
            modifiers: List[LogModifier] = Nil): LogHandler = {
    SynchronousLogHandler(formatter, writer, (LevelFilter >= minimumLevel) :: modifiers)
  }

  def apply(minimumLevel: Level)(f: LogRecord => Unit): LogHandler = {
    FunctionalLogHandler(f, List(LevelFilter >= minimumLevel))
  }
}