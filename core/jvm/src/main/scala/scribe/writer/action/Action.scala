package scribe.writer.action

import scribe.util.Time
import scribe.writer.file.LogFile

import scala.concurrent.duration.FiniteDuration

trait Action {
  def apply(previous: LogFile, current: LogFile): LogFile

  @volatile private var lastCall: Long = 0L
  protected def rateDelayed(rate: FiniteDuration, current: LogFile)(f: => LogFile): LogFile = {
    val now = Time()
    if (now - lastCall >= rate.toMillis) {
      lastCall = now
      f
    } else {
      current
    }
  }
}

object Action {
  def apply(actions: List[Action], previous: LogFile, current: LogFile): LogFile = {
    if (actions.isEmpty) {
      current
    } else {
      val action = actions.head
      val updated = action(previous, current)
      apply(actions.tail, previous, updated)
    }
  }
}