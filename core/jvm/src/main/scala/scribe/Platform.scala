package scribe

import java.io.FileDescriptor

import jnr.posix.POSIXFactory
import scribe.writer.{ANSIConsoleWriter, ASCIIConsoleWriter, ConsoleWriter, ContentSupport, Writer}

object Platform extends PlatformImplementation {
  def isJVM: Boolean = true
  def isJS: Boolean = false
  def isNative: Boolean = false

  private val isAtty: Boolean = POSIXFactory.getPOSIX.isatty(FileDescriptor.out)

  def contentSupport(): ContentSupport = if (isAtty) ContentSupport.Rich else ContentSupport.PlainText

  override def consoleWriter: Writer = ConsoleWriter.contentSupport match {
    case ContentSupport.PlainText => ASCIIConsoleWriter
    case ContentSupport.Rich => ANSIConsoleWriter
  }
}