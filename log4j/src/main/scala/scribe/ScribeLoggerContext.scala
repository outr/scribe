package scribe

import org.apache.logging.log4j.message.MessageFactory
import org.apache.logging.log4j.spi.{ExtendedLogger, LoggerContext, LoggerRegistry}

object ScribeLoggerContext extends LoggerContext {
  private lazy val registry = new LoggerRegistry[ExtendedLogger]()

  override def getExternalContext: AnyRef = None.orNull

  override def getLogger(name: String): ExtendedLogger = {
    if (registry.hasLogger(name)) {
      registry.getLogger(name)
    } else {
      val logger = Logger(name)
      val l = Log4JLogger(logger.id)
      registry.putIfAbsent(name, None.orNull, l)
      l
    }
  }

  override def getLogger(name: String, messageFactory: MessageFactory): ExtendedLogger = {
    if (registry.hasLogger(name, messageFactory)) {
      registry.getLogger(name, messageFactory)
    } else {
      val logger = Logger(name)
      val l = Log4JLogger(logger.id)
      registry.putIfAbsent(name, messageFactory, l)
      l
    }
  }

  override def hasLogger(name: String): Boolean = registry.hasLogger(name)

  override def hasLogger(name: String,
                         messageFactory: MessageFactory): Boolean = registry.hasLogger(name, messageFactory)

  override def hasLogger(name: String,
                         messageFactoryClass: Class[_ <: MessageFactory]): Boolean =
    registry.hasLogger(name, messageFactoryClass)
}