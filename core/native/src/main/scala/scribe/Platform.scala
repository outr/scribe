package scribe

import scribe.writer.{ANSIConsoleWriter, ASCIIConsoleWriter, ConsoleWriter, ContentSupport, Writer}

object Platform extends PlatformImplementation {
  def isJVM: Boolean = false
  def isJS: Boolean = false
  def isNative: Boolean = true

  def contentSupport(): ContentSupport = ContentSupport.Rich    // TODO: Support detection

  override def consoleWriter: Writer = ConsoleWriter.contentSupport match {
    case ContentSupport.PlainText => ASCIIConsoleWriter
    case ContentSupport.Rich => ANSIConsoleWriter
  }
}