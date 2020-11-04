package scribe

import java.io.FileDescriptor

import jnr.posix.POSIXFactory
import scribe.writer.{ANSIConsoleWriter, ASCIIConsoleWriter, ConsoleWriter, Writer}

object Platform extends PlatformImplementation {
  def isJVM: Boolean = true
  def isJS: Boolean = false
  def isNative: Boolean = false

  private val posix = POSIXFactory.getPOSIX
  private val isAtty: Boolean = posix.isatty(FileDescriptor.out)

  override def consoleWriter: Writer = if (!ConsoleWriter.OnlyPlainText && isAtty) {
    ANSIConsoleWriter
  } else {
    ASCIIConsoleWriter
  }
}