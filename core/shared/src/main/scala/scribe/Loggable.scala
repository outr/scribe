package scribe

import perfolation._
import scribe.output.{EmptyOutput, LogOutput, TextOutput}

trait Loggable[-T] {
  def apply(value: T): LogOutput
}

object Loggable {
  implicit object StringLoggable extends Loggable[String] {
    def apply(value: String): LogOutput = new TextOutput(value)
  }

  implicit object LogOutputLoggable extends Loggable[LogOutput] {
    override def apply(value: LogOutput): LogOutput = value
  }

  implicit object PositionListLoggable extends Loggable[List[Position]] {
    override def apply(value: List[Position]): LogOutput = {
      new TextOutput(value.reverse.map(_.toString).mkString(s"${scribe.lineSeparator}\t"))
    }
  }

  implicit object ThrowableLoggable extends Loggable[Throwable] {
    def apply(t: Throwable): LogOutput = LogRecord.throwable2LogOutput(EmptyOutput, t)
  }
}
