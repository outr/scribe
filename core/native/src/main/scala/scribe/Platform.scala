package scribe

import scribe.output.format.{ANSIOutputFormat, ASCIIOutputFormat, OutputFormat}
import scribe.writer.{SystemWriter, Writer}

import scala.concurrent.ExecutionContext

object Platform extends PlatformImplementation {
  def isJVM: Boolean = false
  def isJS: Boolean = false
  def isNative: Boolean = true

  def init(): Unit = {}

  lazy val supportsANSI: Boolean = sys.env.contains("TERM")

  def outputFormat(): OutputFormat = sys.env.get("SCRIBE_OUTPUT_FORMAT").map(_.toUpperCase) match {
    case Some("ANSI") => ANSIOutputFormat
    case Some("ASCII") => ASCIIOutputFormat
    case None if supportsANSI => ANSIOutputFormat
    case None => ASCIIOutputFormat
    case f =>
      System.err.println(s"Unexpected output format specified in SCRIBE_OUTPUT_FORMAT: $f, using ASCII")
      ASCIIOutputFormat
  }

  override def consoleWriter: Writer = SystemWriter

  override val columns: Int = 120 + columnsAdjust

  override def rows: Int = -1

  override def executionContext: ExecutionContext = ExecutionContext.global
}