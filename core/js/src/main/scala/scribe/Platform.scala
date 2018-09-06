package scribe

import scala.scalajs.js

object Platform {
  def isJVM: Boolean = false
  def isJS: Boolean = true
  def isNative: Boolean = false
  
  def console: JavaScriptConsole = js.Dynamic.global.window.console.asInstanceOf[JavaScriptConsole]
}