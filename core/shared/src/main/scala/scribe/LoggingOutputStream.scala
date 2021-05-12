package scribe

import java.io.OutputStream

class LoggingOutputStream(loggerId: LoggerId,
                          level: Level,
                          className: String,
                          methodName: Option[String]) extends OutputStream {
  private lazy val b = new StringBuilder

  override def write(byte: Int): Unit = byte.toChar match {
    case '\n' => {
      Logger(loggerId).logDirect(level, b.toString(), className = className, methodName = methodName)
      b.clear()
    }
    case c => b.append(c)
  }
}