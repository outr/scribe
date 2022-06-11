package scribe.message

import scribe.{LogRecord, Loggable}
import scribe.output.{EmptyOutput, LogOutput, TextOutput}

import scala.language.implicitConversions

trait LoggableMessage {
  def value: Any
  def logOutput: LogOutput
}

object LoggableMessage {
  implicit def string2Message(s: => String): LoggableMessage = apply(s)(new TextOutput(_))
  implicit def stringList2Messages(list: => List[String]): List[LoggableMessage] = list.map(f => string2Message(f))
  implicit def throwable2Message(throwable: => Throwable): LoggableMessage =
    apply(throwable)(LogRecord.throwable2LogOutput(EmptyOutput, _))
  implicit def throwableList2Messages(list: List[Throwable]): List[LoggableMessage] =
    list.map(f => throwable2Message(f))

  def apply[V](value: => V)(toLogOutput: V => LogOutput): LoggableMessage =
    new LazyMessage[V](() => value)(toLogOutput(_))
}