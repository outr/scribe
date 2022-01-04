package scribe

import scribe.writer.Writer

trait PlatformImplementation {
  def isJVM: Boolean
  def isJS: Boolean
  def isNative: Boolean

  def consoleWriter: Writer

  def columns: Int
}