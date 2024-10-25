package scribe

import scribe.message.LoggableMessage
import scribe.output.LogOutput
import scribe.throwable.TraceLoggableMessage

import scala.language.implicitConversions

trait LogFeature {
  def apply(record: LogRecord): LogRecord
}

object LogFeature {
  def apply(f: LogRecord => LogRecord): LogFeature = (record: LogRecord) => f(record)

  implicit def stringFunc2LoggableMessage(f: () => String): LogFeature = LoggableMessage.string2LoggableMessage(f())
  implicit def string2LoggableMessage(s: => String): LogFeature = LoggableMessage.string2LoggableMessage(s)
  implicit def logOutput2LoggableMessage(lo: => LogOutput): LogFeature = LoggableMessage[LogOutput](identity)(lo)
  implicit def throwable2LoggableMessage(throwable: => Throwable): LogFeature = TraceLoggableMessage(throwable)
}