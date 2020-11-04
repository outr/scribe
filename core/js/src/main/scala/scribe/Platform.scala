package scribe

import scribe.writer.{BrowserConsoleWriter, ContentSupport, Writer}

import scala.scalajs.js

object Platform extends PlatformImplementation {
  def isJVM: Boolean = false
  def isJS: Boolean = true
  def isNative: Boolean = false
  
  def console: JavaScriptConsole = js.Dynamic.global.console.asInstanceOf[JavaScriptConsole]

  def contentSupport(): ContentSupport = ContentSupport.Rich

  override def consoleWriter: Writer = BrowserConsoleWriter
}