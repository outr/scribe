package scribe.util

import java.time.{Instant, LocalDateTime, ZoneId}

object LastLocalDateTime {
  private val l = new ThreadLocal[Long] {
    override def initialValue(): Long = 0L
  }
  private val ldt = new ThreadLocal[LocalDateTime]

  def apply(l: Long): LocalDateTime = if (this.l.get() == l) {
    ldt.get()
  } else {
    val ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(l), ZoneId.systemDefault())
    this.l.set(l)
    this.ldt.set(ldt)
    ldt
  }
}