package scribe

import scribe.writer.{BrowserConsoleWriter, Writer}

import scala.scalajs.js

object Platform extends PlatformImplementation {
  def isJVM: Boolean = false
  def isJS: Boolean = true
  def isNative: Boolean = false
  
  def console: JavaScriptConsole = js.Dynamic.global.console.asInstanceOf[JavaScriptConsole]

  override def consoleWriter: Writer = BrowserConsoleWriter
}