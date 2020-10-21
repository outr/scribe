package scribe

import scribe.writer.{ANSIConsoleWriter, ASCIIConsoleWriter, ConsoleWriter, Writer}

object Platform extends PlatformImplementation {
  def isJVM: Boolean = false
  def isJS: Boolean = false
  def isNative: Boolean = true

  override def consoleWriter: Writer = if (ConsoleWriter.OnlyPlainText) {
    ASCIIConsoleWriter
  } else {
    ANSIConsoleWriter
  }
}