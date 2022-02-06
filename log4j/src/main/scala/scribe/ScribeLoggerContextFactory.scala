package scribe

import org.apache.logging.log4j.spi.{LoggerContext, LoggerContextFactory}

import java.net.URI

class ScribeLoggerContextFactory extends LoggerContextFactory {
  override def getContext(fqcn: String,
                          loader: ClassLoader,
                          externalContext: Any,
                          currentContext: Boolean): LoggerContext = ScribeLoggerContext

  override def getContext(fqcn: String,
                          loader: ClassLoader,
                          externalContext: Any,
                          currentContext: Boolean,
                          configLocation: URI,
                          name: String): LoggerContext = ScribeLoggerContext

  override def removeContext(context: LoggerContext): Unit = {}
}