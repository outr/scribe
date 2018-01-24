package scribe

import scribe.format.Formatter
import scribe.modify.LogModifier
import scribe.writer.{ConsoleWriter, Writer}

case class LogHandler(formatter: Formatter = Formatter.default,
                      writer: Writer = ConsoleWriter,
                      modifiers: List[LogModifier] = Nil) extends LogSupport {
  override type Self = Logger

  def withFormatter(formatter: Formatter): LogHandler = copy(formatter = formatter)
  def withWriter(writer: Writer): LogHandler = copy(writer = writer)
  override def withModifier(modifier: LogModifier): LogHandler = copy(modifiers = modifiers ::: List(modifier))
  override def withoutModifier(modifier: LogModifier): LogHandler = copy(modifiers = modifiers.filterNot(_ == modifier))

  def log(record: LogRecord): Unit = {
    modifiers.foldLeft(Option(record))((r, lm) => r.flatMap(lm.apply)).foreach { r =>
      writer.write(formatter.format(r))
    }
  }
}

object LogHandler {
  lazy val default: LogHandler = LogHandler()
}