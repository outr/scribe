package scribe

import scribe.message.{LoggableMessage, Message}
import scribe.output.{EmptyOutput, LogOutput, TextOutput}

import scala.language.implicitConversions

trait Loggable[-T] {
  def apply(value: T): LogOutput
}