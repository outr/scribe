package scribe.slf4j

import java.util.concurrent.ConcurrentHashMap
import org.slf4j.{ILoggerFactory, Logger, ScribeLoggerAdapter}

class ScribeLoggerFactory extends ILoggerFactory {
  private val map = new ConcurrentHashMap[String, Logger]

  override def getLogger(name: String): Logger = {
    val loggerName = if (name.equalsIgnoreCase(Logger.ROOT_LOGGER_NAME)) {
      ""
    } else {
      name
    }
    Option(map.get(loggerName)) match {
      case Some(logger) => logger
      case None => {
        val adapter = new ScribeLoggerAdapter(loggerName)
        val old = map.putIfAbsent(loggerName, adapter)
        Option(old) match {
          case Some(a) => a
          case None => adapter
        }
      }
    }
  }
}
