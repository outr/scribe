package scribe

import scribe.writer.{ANSIConsoleWriter, Writer}

object Platform extends PlatformImplementation {
  def isJVM: Boolean = true
  def isJS: Boolean = false
  def isNative: Boolean = false

  override def consoleWriter: Writer = ANSIConsoleWriter
}