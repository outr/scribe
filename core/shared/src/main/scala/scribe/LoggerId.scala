package scribe

final case class LoggerId(val value: Long) extends AnyVal

object LoggerId {
  def apply(): LoggerId = LoggerId(scala.util.Random.nextLong())
}