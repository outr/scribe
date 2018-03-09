package scribe

import scala.scalajs.js.Date

object Platform {
  val startOfYear: Date = new Date(new Date().getFullYear(), 0, 0)
  val lineSeparator: String = "\n"

  def createDate(l: Long): CrossDate = new JavaScriptCrossDate(l, new Date(l.toDouble))
}