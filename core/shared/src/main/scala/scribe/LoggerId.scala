package scribe

import java.util.concurrent.atomic.AtomicLong

final case class LoggerId(value: Long) extends AnyVal

object LoggerId {
  private val counter = new AtomicLong(0L)

  def apply(): LoggerId = new LoggerId(counter.incrementAndGet())
}