package scribe

import scala.language.implicitConversions

package object mdc {
  implicit def any2Value(value: => Any): MDCValue = this.value(value)

  def static(value: Any): MDCValue = MDCValue(() => value)
  def value(value: => Any): MDCValue = MDCValue(() => value)
}