package scribe

import scribe.writer.{ANSIConsoleWriter, Writer}

object Platform extends PlatformImplementation {
  def isJVM: Boolean = false
  def isJS: Boolean = false
  def isNative: Boolean = true

  override def consoleWriter: Writer = ANSIConsoleWriter
}
