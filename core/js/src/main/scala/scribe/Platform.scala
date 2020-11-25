package scribe

import scribe.output.format.{OutputFormat, RichBrowserOutputFormat}
import scribe.writer.{BrowserConsoleWriter, Writer}

import scala.scalajs.js

object Platform extends PlatformImplementation {
  def isJVM: Boolean = false
  def isJS: Boolean = true
  def isNative: Boolean = false

  def init(): Unit = {}
  
  def console: JavaScriptConsole = js.Dynamic.global.console.asInstanceOf[JavaScriptConsole]

  def outputFormat(): OutputFormat = RichBrowserOutputFormat

  override def consoleWriter: Writer = BrowserConsoleWriter
}