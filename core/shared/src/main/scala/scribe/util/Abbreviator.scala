package scribe.util

import perfolation._

object Abbreviator {
  def apply(value: String,
            maxLength: Int,
            separator: Char = '.',
            removeEntries: Boolean = true,
            abbreviateName: Boolean = false): String = {
    var entries = value.split(separator).filter(_.nonEmpty)
    def result = entries.mkString(separator.toString)
    var position = 0
    while (result.length > maxLength && position < entries.length - 1) {
      entries(position) = entries(position).charAt(0).toString
      position += 1
    }
    if (result.length > maxLength && removeEntries) {
      entries = Array(entries.last)
    }
    if (result.length > math.max(maxLength, 4) && abbreviateName) {
      val entry = entries.head
      val offset = math.max(maxLength - 3, 1)
      entries = Array(p"${entry.substring(0, offset)}...")
    }
    result
  }
}