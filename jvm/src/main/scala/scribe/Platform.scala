package scribe

object Platform {
  val lineSeparator = System.getProperty("line.separator")

  def formatDate(pattern: String, timestamp: Long): String = pattern.format(timestamp)
}