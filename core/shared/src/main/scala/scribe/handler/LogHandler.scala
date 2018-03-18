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
            minimumLevel: Option[Level] = None,
            modifiers: List[LogModifier] = Nil): LogHandler = {
    val mods = minimumLevel.map(LevelFilter >= _).toList ::: modifiers
    SynchronousLogHandler(formatter, writer, mods)
  }

  def apply(minimumLevel: Level)(f: LogRecord[_] => Unit): LogHandler = {
    FunctionalLogHandler(f, List(LevelFilter >= minimumLevel))
  }
}