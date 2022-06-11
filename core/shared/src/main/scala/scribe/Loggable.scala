package scribe

import scribe.message.{LoggableMessage, Message}
import scribe.output.{EmptyOutput, LogOutput, TextOutput}

import scala.language.implicitConversions

trait Loggable[-T] {
  def apply(value: T): LogOutput
}

object Loggable {
  def apply[V](f: V => LogOutput): V => LoggableMessage = (v: V) => {
    LoggableMessage[V](v)(f)
  }
}