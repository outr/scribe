package scribe

import scribe.output.LogOutput

import scala.language.implicitConversions

trait Loggable[-T] {
  def apply(value: T): LogOutput
}