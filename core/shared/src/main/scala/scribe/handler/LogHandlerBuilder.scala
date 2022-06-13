package scribe.handler

import scribe.LogRecord
import scribe.format.Formatter
import scribe.modify.LogModifier
import scribe.output.format.OutputFormat
import scribe.writer.{ConsoleWriter, Writer}

case class LogHandlerBuilder(formatter: Formatter = Formatter.default,
                             writer: Writer = ConsoleWriter,
                             outputFormat: OutputFormat = OutputFormat.default,
                             modifiers: List[LogModifier] = Nil,
                             handle: LogHandle = SynchronousLogHandle) extends LogHandler {
  override def log(record: LogRecord): Unit = handle.log(this, record)

  def withFormatter(formatter: Formatter): LogHandlerBuilder = copy(formatter = formatter)

  def withWriter(writer: Writer): LogHandlerBuilder = copy(writer = writer)

  def withModifiers(modifiers: LogModifier*): LogHandlerBuilder = copy(modifiers = modifiers.toList ::: this.modifiers)

  def withLogHandle(handle: LogHandle): LogHandlerBuilder = copy(handle = handle)
}