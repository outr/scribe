package scribe.writer.action

import scribe.writer.file.LogFile

import scala.concurrent.duration.FiniteDuration

case class MaxLogSizeAction(maxSizeInBytes: Long,
                            action: Action,
                            checkRate: FiniteDuration) extends Action {
  override def apply(previous: LogFile, current: LogFile): LogFile = rateDelayed(checkRate, current) {
    if (current.size >= maxSizeInBytes) {
      Action(List(action), previous, current)
    } else {
      current
    }
  }
}