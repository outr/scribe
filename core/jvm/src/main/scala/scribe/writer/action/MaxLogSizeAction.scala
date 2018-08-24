package scribe.writer.action

import scribe.writer.file.LogFile

import scala.concurrent.duration.FiniteDuration

case class MaxLogSizeAction(maxSizeInBytes: Long,
                            actions: List[Action],
                            checkRate: FiniteDuration) extends Action {
  override def apply(previous: LogFile, current: LogFile): LogFile = rateDelayed(checkRate, current) {
    if (current.size >= maxSizeInBytes) {
      Action(actions, previous, current)
    } else {
      current
    }
  }
}
