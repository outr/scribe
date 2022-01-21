package scribe.slf4j

import java.util.concurrent.ConcurrentHashMap
import org.slf4j.{ILoggerFactory, Logger, ScribeLoggerAdapter}

object ScribeLoggerFactory extends ILoggerFactory {
  private lazy val map = new ConcurrentHashMap[String, Logger]()

  override def getLogger(name: String): Logger = Option(map.get(name)) match {
    case Some(logger) => logger
    case None =>
      val logger = new ScribeLoggerAdapter(name)
      val oldInstance = map.putIfAbsent(name, logger)
      Option(oldInstance).getOrElse(logger)
  }
}
