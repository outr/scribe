package scribe.handler

import scribe.format.Formatter
import scribe.modify.{LevelFilter, LogModifier}
import scribe.writer.{ConsoleWriter, Writer}
import scribe.{Level, LogRecord}

/**
 * LogHandler is responsible for causing some side-effect with a `LogRecord`. This usually includes formatting the record
 * with a `Formatter` and writing it to a `Writer`, although some more creative implementations exist to do more advanced
 * actions. LogHandlers are added to `Logger` instances via `withHandler`, although it's usually sufficient to use the
 * `withHandler` method that takes a `Formatter` and `Writer` instead of defining a `LogHandler` manually.
 */
trait LogHandler {
  def log[M](record: LogRecord[M]): Unit
}

object LogHandler {
  lazy val default: SynchronousLogHandler = SynchronousLogHandler()

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