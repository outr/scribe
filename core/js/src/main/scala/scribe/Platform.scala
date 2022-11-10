package scribe

import scribe.output.format.{OutputFormat, RichBrowserOutputFormat}
import scribe.writer.{BrowserConsoleWriter, Writer}

import scala.concurrent.ExecutionContext
import scala.scalajs.js

object Platform extends PlatformImplementation {
  def isJVM: Boolean = false
  def isJS: Boolean = true
  def isNative: Boolean = false

  def init(): Unit = {}

  // $COVERAGE-OFF$
  def console: JavaScriptConsole = js.Dynamic.global.console.asInstanceOf[JavaScriptConsole]
  // $COVERAGE-ON$

  def outputFormat(): OutputFormat = RichBrowserOutputFormat

  override def consoleWriter: Writer = BrowserConsoleWriter

  override val columns: Int = 120 + columnsAdjust

  override def rows: Int = -1

  override def executionContext: ExecutionContext = scala.scalajs.concurrent.JSExecutionContext.queue
}