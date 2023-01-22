package scribe

import scribe.output.format.{ANSIOutputFormat, ASCIIOutputFormat, OutputFormat, RichBrowserOutputFormat}
import scribe.writer.{BrowserConsoleWriter, Writer}

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.scalajs.js.Dictionary
import scala.util.Try

object Platform extends PlatformImplementation {
  def isJVM: Boolean = false
  def isJS: Boolean = true
  def isNative: Boolean = false

  lazy val isNodeJS: Boolean = Try(js.Dynamic.global.process.release.name.asInstanceOf[String]).toOption.contains("node")

  def init(): Unit = {}

  // $COVERAGE-OFF$
  def console: JavaScriptConsole = js.Dynamic.global.console.asInstanceOf[JavaScriptConsole]
  // $COVERAGE-ON$

  private def processEnv: Dictionary[Any] = Try(js.Dynamic.global.process.env.asInstanceOf[js.Dictionary[Any]])
    .getOrElse(js.Dictionary.empty)

  override def env(key: String): Option[String] = processEnv.get(key).map(_.toString)

  override def outputFormat(): OutputFormat = if (isNodeJS) {
    super.outputFormat()
  } else {
    RichBrowserOutputFormat
  }

  override def consoleWriter: Writer = BrowserConsoleWriter

  override val columns: Int = 120 + columnsAdjust

  override def rows: Int = -1

  override def executionContext: ExecutionContext = scala.scalajs.concurrent.JSExecutionContext.queue
}