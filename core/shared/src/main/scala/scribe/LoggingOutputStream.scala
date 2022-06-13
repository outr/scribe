package scribe

import java.io.OutputStream
import scala.collection.mutable

class LoggingOutputStream(loggerId: LoggerId,
                          level: Level,
                          className: String,
                          methodName: Option[String]) extends OutputStream {
  private lazy val b = new mutable.StringBuilder

  override def write(byte: Int): Unit = byte.toChar match {
    case '\n' => {
      Logger(loggerId).logDirect(level, List(b.toString()), className = className, methodName = methodName)
      b.clear()
    }
    case c => b.append(c)
  }
}