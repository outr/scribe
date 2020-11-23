package scribe.output.format

import scribe.Platform
import scribe.output.LogOutput

trait OutputFormat {
  def apply(output: LogOutput, stream: String => Unit): Unit
}

object OutputFormat {
  /**
    * Defaults to platform-specific format.
    */
  var default: OutputFormat = Platform.outputFormat()
}