package scribe.message

import scribe.output.{LogOutput, TextOutput}
import scribe.throwable.TraceLoggableMessage
import scribe.{LogFeature, LogRecord, Loggable}

import scala.language.implicitConversions

trait LoggableMessage extends LogFeature {
  def value: Any
  def logOutput: LogOutput

  override def apply(record: LogRecord): LogRecord = record.withMessages(this)
}

object LoggableMessage {
  implicit def string2LoggableMessage(s: => String): LoggableMessage = LoggableMessage[String](new TextOutput(_))(s)
  implicit def stringList2Messages(list: => List[String]): List[LoggableMessage] =
    list.map(f => string2LoggableMessage(f))
  implicit def throwableList2Messages(list: List[Throwable]): List[LoggableMessage] =
    list.map(f => TraceLoggableMessage(f))

  def apply[V](toLogOutput: V => LogOutput)(value: => V): LoggableMessage =
    new LazyMessage[V](() => value)(new Loggable[V] {
      override def apply(value: V): LogOutput = toLogOutput(value)
    })
}