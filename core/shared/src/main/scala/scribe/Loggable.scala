package scribe

trait Loggable[T] {
  def apply(value: T): String
}

object Loggable {
  implicit object StringLoggable extends Loggable[String] {
    def apply(value: String): String = value
  }
}
