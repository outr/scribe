package scribe

import perfolation._

trait Loggable[-T] {
  def apply(value: T): String
}

object Loggable {
  implicit object StringLoggable extends Loggable[String] {
    def apply(value: String): String = value
  }

  implicit object PositionListLoggable extends Loggable[List[Position]] {
    override def apply(value: List[Position]): String = {
      value.reverse.map(_.toString).mkString(p"${scribe.lineSeparator}\t")
    }
  }

  implicit object ThrowableLoggable extends Loggable[Throwable] {
    def apply(t: Throwable): String = LogRecord.throwable2String(None, t)
  }
}
