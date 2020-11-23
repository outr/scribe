package scribe

import scribe.output.format.{ANSIOutputFormat, OutputFormat}
import scribe.writer.{SystemOutputWriter, Writer}

object Platform extends PlatformImplementation {
  def isJVM: Boolean = true
  def isJS: Boolean = false
  def isNative: Boolean = false

  def outputFormat(): OutputFormat = ANSIOutputFormat

  override def consoleWriter: Writer = SystemOutputWriter
}