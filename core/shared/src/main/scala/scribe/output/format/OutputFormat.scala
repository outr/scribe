package scribe.output.format

import scribe.Platform
import scribe.output.LogOutput

trait OutputFormat {
  def begin(stream: String => Unit): Unit = {}

  def apply(output: LogOutput, stream: String => Unit): Unit

  def end(stream: String => Unit): Unit = {}
}

object OutputFormat {
  /**
    * Defaults to platform-specific format.
    */
  var default: OutputFormat = Platform.outputFormat()
}