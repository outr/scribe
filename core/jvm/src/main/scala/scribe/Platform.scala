package scribe

import java.io.FileDescriptor

import jnr.posix.POSIXFactory
import scribe.writer.{ANSIConsoleWriter, ASCIIConsoleWriter, Writer}

object Platform extends PlatformImplementation {
  def isJVM: Boolean = true
  def isJS: Boolean = false
  def isNative: Boolean = false

  private lazy val posix = POSIXFactory.getPOSIX

  override def consoleWriter: Writer = if (posix.isatty(FileDescriptor.out)) {
    ANSIConsoleWriter
  } else {
    ASCIIConsoleWriter
  }
}