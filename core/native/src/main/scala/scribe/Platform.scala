package scribe

import scribe.output.format.{ANSIOutputFormat, OutputFormat}
import scribe.writer.{SystemOutputWriter, Writer}

object Platform extends PlatformImplementation {
  def isJVM: Boolean = false
  def isJS: Boolean = false
  def isNative: Boolean = true

  def init(): Unit = {}

  def outputFormat(): OutputFormat = ANSIOutputFormat

  override def consoleWriter: Writer = SystemOutputWriter
}