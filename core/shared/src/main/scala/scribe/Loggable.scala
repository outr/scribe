package scribe

trait Loggable[-T] {
  def apply(value: T): String
}

object Loggable {
  implicit object StringLoggable extends Loggable[String] {
    def apply(value: String): String = value
  }

  implicit object ThrowableLoggable extends Loggable[Throwable] {
    def apply(t: Throwable): String = LogRecord.throwable2String(None, t)
  }
}
