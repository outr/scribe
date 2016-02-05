package com.outr.scribe

import org.widok.moment.Moment

object Platform {
  val LineSeparator = "\n"

  // TODO Don't ignore pattern
  def formatDate(pattern: String, timestamp: Long): String =
    Moment(timestamp).format()
}