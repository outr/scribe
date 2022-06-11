package scribe.message

import scribe.output.{EmptyOutput, LogOutput}

object EmptyMessage extends Message[String] {
  override val value: String = ""
  override val logOutput: LogOutput = EmptyOutput
}