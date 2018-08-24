package scribe.writer.action

import scribe.util.Time
import scribe.writer.file.LogFile

trait Action {
  def apply(previous: LogFile, current: LogFile): LogFile

  @volatile private var lastCall: Long = 0L
  protected def rateDelayed(rate: Long, current: LogFile)(f: => LogFile): LogFile = {
    val now = Time()
    if (now - lastCall >= rate) {
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