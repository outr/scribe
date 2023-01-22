package scribe

import scribe.output.format.{ANSIOutputFormat, ASCIIOutputFormat, OutputFormat}
import scribe.writer.{SystemWriter, Writer}

import scala.concurrent.ExecutionContext

object Platform extends PlatformImplementation {
  def isJVM: Boolean = false
  def isJS: Boolean = false
  def isNative: Boolean = true

  def init(): Unit = {}

  override def consoleWriter: Writer = SystemWriter

  override val columns: Int = 120 + columnsAdjust

  override def rows: Int = -1

  override def executionContext: ExecutionContext = ExecutionContext.global
}