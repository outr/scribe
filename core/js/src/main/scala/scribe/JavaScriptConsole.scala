package scribe

import scala.scalajs.js

/**
  * Facade around extra features of the JavaScript console in the browser
  */
@js.native
trait JavaScriptConsole extends js.Object {

  /**
    * Outputs an informational message to the Web Console. In Firefox, a small "i" icon is
    * displayed next to these items in the Web Console's log.
    *
    * MDN
    */
  def info(message: js.Any, optionalParams: js.Any*): Unit = js.native

  def profile(reportName: String = js.native): Unit = js.native

  def assert(test: Boolean, message: String,
             optionalParams: js.Any*): Unit = js.native

  def clear(): Unit = js.native

  /**
    * Displays an interactive list of the properties of the specified JavaScript
    * object. The output is presented as a hierarchical listing with disclosure
    * triangles that let you see the contents of child objects.
    *
    * MDN
    */
  def dir(value: js.Any, optionalParams: js.Any*): Unit = js.native

  /**
    * Outputs a warning message. You may use string substitution and additional
    * arguments with this method. See Using string substitutions.
    *
    * MDN
    */
  def warn(message: js.Any, optionalParams: js.Any*): Unit = js.native

  /**
    * Outputs an error message. You may use string substitution and additional
    * arguments with this method. See Using string substitutions.
    *
    * MDN
    */
  def error(message: js.Any, optionalParams: js.Any*): Unit = js.native

  /**
    * For general output of logging information. You may use string substitution and
    * additional arguments with this method. See Using string substitutions.
    *
    * MDN
    */
  def log(message: js.Any, optionalParams: js.Any*): Unit = js.native

  def profileEnd(): Unit = js.native
}
