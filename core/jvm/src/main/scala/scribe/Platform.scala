package scribe

object Platform {
  val lineSeparator: String = System.getProperty("line.separator")

  def isJS: Boolean = false
}