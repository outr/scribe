package scribe.message

import scribe.Loggable
import scribe.output.LogOutput

case class StaticMessage[M](value: M)
                           (implicit loggable: Loggable[M]) extends Message[M] {
  override lazy val logOutput: LogOutput = loggable(value)
}