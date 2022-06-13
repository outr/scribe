package scribe.message

import scribe.{LogRecord, Loggable}
import scribe.output.{EmptyOutput, LogOutput, TextOutput}

import scala.language.implicitConversions

trait LoggableMessage {
  def value: Any
  def logOutput: LogOutput
}

object LoggableMessage {
  implicit def string2Message(s: => String): LoggableMessage = apply[String](new TextOutput(_))(s)
  implicit def stringList2Messages(list: => List[String]): List[LoggableMessage] = list.map(f => string2Message(f))
  implicit def throwable2Message(throwable: => Throwable): LoggableMessage =
    apply[Throwable](LogRecord.throwable2LogOutput(EmptyOutput, _))(throwable)
  implicit def throwableList2Messages(list: List[Throwable]): List[LoggableMessage] =
    list.map(f => throwable2Message(f))

  def apply[V](toLogOutput: V => LogOutput)(value: => V): LoggableMessage =
    new LazyMessage[V](() => value)(new Loggable[V] {
      override def apply(value: V): LogOutput = toLogOutput(value)
    })
}