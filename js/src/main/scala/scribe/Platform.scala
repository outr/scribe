package scribe

import scala.scalajs.js.Date

object Platform {
  val lineSeparator = "\n"

  private val replacements = Map(
    "%1$tY" -> ((d: Date) => d.getFullYear().toString),
    "%1$tm" -> ((d: Date) => twoPlaces(d.getMonth() + 1).toString),
    "%1$td" -> ((d: Date) => d.getDate().toString),
    "%1$tT" -> ((d: Date) => s"${d.getHours()}:${twoPlaces(d.getMinutes())}:${twoPlaces(d.getSeconds())}"),
    "%1$tL" -> ((d: Date) => d.getMilliseconds().toString)
  )

  private def twoPlaces(value: Int): String = {
    if (value < 10) {
      s"0$value"
    } else {
      value.toString
    }
  }

  def formatDate(pattern: String, timestamp: Long): String = {
    val date = new Date(timestamp.toDouble)
    val standardPattern = replacements.foldLeft(pattern) {
      case (p, (original, update)) => p.replaceAllLiterally(original, update(date))
    }
    date.formatted(standardPattern)
  }
}