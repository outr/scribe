package scribe

import scribe.output.format.{ANSIOutputFormat, ASCIIOutputFormat, OutputFormat}
import scribe.writer.Writer

import scala.concurrent.ExecutionContext

trait PlatformImplementation {
  var columnsAdjust: Int = 0

  def isJVM: Boolean
  def isJS: Boolean
  def isNative: Boolean

  def consoleWriter: Writer

  def columns: Int
  def rows: Int

  def executionContext: ExecutionContext

  def env(key: String): Option[String] = sys.env.get(key)

  lazy val supportsANSI: Boolean = env("TERM").nonEmpty

  def outputFormat(): OutputFormat = env("SCRIBE_OUTPUT_FORMAT").map(_.toUpperCase) match {
    case Some("ANSI") => ANSIOutputFormat
    case Some("ASCII") => ASCIIOutputFormat
    case None if supportsANSI => ANSIOutputFormat
    case None => ASCIIOutputFormat
    case f =>
      System.err.println(s"Unexpected output format specified in SCRIBE_OUTPUT_FORMAT: $f, using ASCII")
      ASCIIOutputFormat
  }
}