package scribe

import scribe.writer.{ANSIConsoleWriter, ASCIIConsoleWriter, ConsoleWriter, ContentSupport, Writer}

object Platform extends PlatformImplementation {
  def isJVM: Boolean = true
  def isJS: Boolean = false
  def isNative: Boolean = false

  def contentSupport(): ContentSupport = ContentSupport.Rich    // TODO: figure out a way to detect without using POSIX

  override def consoleWriter: Writer = ConsoleWriter.contentSupport match {
    case ContentSupport.PlainText => ASCIIConsoleWriter
    case ContentSupport.Rich => ANSIConsoleWriter
  }
}