package scribe.message

import scribe.output.LogOutput

trait LoggableMessage {
  def logOutput: LogOutput
}