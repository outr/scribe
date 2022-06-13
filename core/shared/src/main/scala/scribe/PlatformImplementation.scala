package scribe

import scribe.writer.Writer

import scala.concurrent.ExecutionContext

trait PlatformImplementation {
  def isJVM: Boolean
  def isJS: Boolean
  def isNative: Boolean

  def consoleWriter: Writer

  def columns: Int

  def executionContext: ExecutionContext
}