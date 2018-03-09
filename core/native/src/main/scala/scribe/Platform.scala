package scribe

import java.util.Calendar

object Platform {
  val lineSeparator: String = System.getProperty("line.separator")

  def createDate(l: Long): CrossDate = {
    val c = Calendar.getInstance()
    c.setTimeInMillis(l)
    new JVMCrossDate(l, c)
  }
}
