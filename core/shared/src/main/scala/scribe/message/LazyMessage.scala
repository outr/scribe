package scribe.message

import scribe.Loggable
import scribe.output.LogOutput

class LazyMessage[M](function: () => M)
                    (implicit loggable: Loggable[M]) extends Message[M] {
  override lazy val value: M = function()
  override lazy val logOutput: LogOutput = loggable(value)
}