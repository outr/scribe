package scribe

import scribe.output.format.{ANSIOutputFormat, ASCIIOutputFormat, OutputFormat}
import scribe.writer.{SystemOutputWriter, Writer}

object Platform extends PlatformImplementation {
  def isJVM: Boolean = false
  def isJS: Boolean = false
  def isNative: Boolean = true

  def init(): Unit = {}

  def outputFormat(): OutputFormat = sys.env.getOrElse("SCRIBE_OUTPUT_FORMAT", "ANSI").toUpperCase match {
    case "ANSI" => ANSIOutputFormat
    case "ASCII" => ASCIIOutputFormat
    case f => {
      scribe.warn(s"Unexpected output format specified in SCRIBE_OUTPUT_FORMAT: $f, using ASCII")
      ASCIIOutputFormat
    }
  }

  override def consoleWriter: Writer = SystemOutputWriter
}