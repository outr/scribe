package scribe

import moduload.Moduload
import scribe.output.format.{ANSIOutputFormat, ASCIIOutputFormat, OutputFormat}
import scribe.writer.{SystemOutputWriter, Writer}

import scala.util.Try

object Platform extends PlatformImplementation {
  def isJVM: Boolean = true
  def isJS: Boolean = false
  def isNative: Boolean = false

  def init(): Unit = {
    // Load Moduload
    Moduload.load()
  }

  lazy val supportsANSI: Boolean = Try(System.console() != null && sys.env.contains("TERM")).getOrElse(false)

  def outputFormat(): OutputFormat = sys.env.get("SCRIBE_OUTPUT_FORMAT").map(_.toUpperCase) match {
    case Some("ANSI") | None if supportsANSI => ANSIOutputFormat
    case Some("ASCII") | None if !supportsANSI => ASCIIOutputFormat
    case f =>
      scribe.warn(s"Unexpected output format specified in SCRIBE_OUTPUT_FORMAT: $f, using ASCII")
      ASCIIOutputFormat
  }

  override def consoleWriter: Writer = SystemOutputWriter
}