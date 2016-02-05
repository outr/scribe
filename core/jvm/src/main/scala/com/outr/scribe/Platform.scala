package com.outr.scribe

object Platform {
  val LineSeparator = System.getProperty("line.separator")

  def formatDate(pattern: String, timestamp: Long): String =
    pattern.format(timestamp)
}