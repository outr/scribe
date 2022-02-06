package scribe

import scribe.message.{LoggableMessage, Message}
import scribe.output.{EmptyOutput, LogOutput, TextOutput}

import scala.language.implicitConversions

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

  implicit object ThrowableLoggable extends Loggable[Throwable] {
    def apply(t: Throwable): LogOutput = LogRecord.throwable2LogOutput(EmptyOutput, t)
  }
}
