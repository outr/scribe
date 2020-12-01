package scribe

import moduload.Moduload
import scribe.output.format.{ANSIOutputFormat, ASCIIOutputFormat, OutputFormat}
import scribe.writer.{SystemOutputWriter, Writer}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Platform extends PlatformImplementation {
  def isJVM: Boolean = true
  def isJS: Boolean = false
  def isNative: Boolean = false

  def init(): Unit = {
    // Load Moduload
    Await.result(Moduload.load()(Execution.global), Duration.Inf)
  }

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