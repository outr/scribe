package scribe

import moduload.Moduload
import scribe.output.format.{ANSIOutputFormat, OutputFormat}
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

  def outputFormat(): OutputFormat = ANSIOutputFormat

  override def consoleWriter: Writer = SystemOutputWriter
}