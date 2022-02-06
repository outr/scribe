package scribe.message

import scribe.Loggable
import scribe.output.LogOutput

object EmptyMessage extends Message[String] {
  override val value: String = ""
  override val logOutput: LogOutput = Loggable.StringLoggable(value)
}