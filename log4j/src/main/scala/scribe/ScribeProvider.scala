package scribe

import org.apache.logging.log4j
import org.apache.logging.log4j.{Marker, message}
import org.apache.logging.log4j.message.MessageFactory
import org.apache.logging.log4j.spi.{AbstractLogger, CleanableThreadContextMap, ExtendedLogger, LoggerContext, LoggerContextFactory, LoggerRegistry, Provider}
import org.apache.logging.log4j.util.{SortedArrayStringMap, StringMap}
import scribe.data.MDC

import scala.jdk.CollectionConverters._
import java.net.URI
import scala.language.implicitConversions

class ScribeProvider extends Provider(15, "2.6.0", classOf[ScribeLoggerContextFactory], classOf[ScribeContextMap])

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

object ScribeLoggerContext extends LoggerContext {
  private lazy val registry = new LoggerRegistry[ExtendedLogger]()

  override def getExternalContext: AnyRef = null

  override def getLogger(name: String): ExtendedLogger = {
    if (registry.hasLogger(name)) {
      registry.getLogger(name)
    } else {
      val logger = Logger(name)
      val l = Log4JLogger(logger.id)
      registry.putIfAbsent(name, null, l)
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

  override def hasLogger(name: String, messageFactory: MessageFactory): Boolean = registry.hasLogger(name, messageFactory)

  override def hasLogger(name: String, messageFactoryClass: Class[_ <: MessageFactory]): Boolean = registry.hasLogger(name, messageFactoryClass)
}

class ScribeContextMap extends CleanableThreadContextMap {
  override def removeAll(keys: java.lang.Iterable[String]): Unit = keys.asScala.foreach(remove)

  override def putAll(map: java.util.Map[String, String]): Unit = map.asScala.foreach {
    case (key, value) => put(key, value)
  }

  override def getReadOnlyContextData: StringMap = {
    val result = new SortedArrayStringMap
    MDC.map.foreach {
      case (key, value) => result.putValue(key, value())
    }
    result
  }

  override def clear(): Unit = MDC.clear()

  override def containsKey(key: String): Boolean = MDC.contains(key)

  override def get(key: String): String = MDC.get(key).map(_.toString).orNull

  override def getCopy: java.util.Map[String, String] = MDC.map.map {
    case (key, value) => key -> value().toString
  }.asJava

  override def getImmutableMapOrNull: java.util.Map[String, String] = getCopy

  override def isEmpty: Boolean = MDC.map.isEmpty

  override def put(key: String, value: String): Unit = MDC(key) = value

  override def remove(key: String): Unit = MDC.remove(key)
}

case class Log4JLogger(id: LoggerId) extends AbstractLogger {
  protected def logger: Logger = Logger(id)

  private implicit def l2l(level: log4j.Level): Option[Level] = level match {
    case log4j.Level.OFF => None
    case log4j.Level.FATAL => Some(Level.Fatal)
    case log4j.Level.ERROR => Some(Level.Error)
    case log4j.Level.WARN => Some(Level.Warn)
    case log4j.Level.INFO => Some(Level.Info)
    case log4j.Level.DEBUG => Some(Level.Debug)
    case log4j.Level.TRACE => Some(Level.Trace)
    case log4j.Level.ALL => None
  }

  private def enabled(level: log4j.Level): Boolean = l2l(level) match {
    case Some(l) => logger.includes(l)
    case None if level == log4j.Level.ALL => true
    case None => false
  }

  override def isEnabled(level: log4j.Level, marker: Marker, message: org.apache.logging.log4j.message.Message, t: Throwable): Boolean =
    enabled(level)

  override def isEnabled(level: log4j.Level, marker: Marker, message: CharSequence, t: Throwable): Boolean =
    enabled(level)

  override def isEnabled(level: log4j.Level, marker: Marker, message: Any, t: Throwable): Boolean =
    enabled(level)

  override def isEnabled(level: log4j.Level, marker: Marker, message: String, t: Throwable): Boolean =
    enabled(level)

  override def isEnabled(level: log4j.Level, marker: Marker, message: String): Boolean =
    enabled(level)

  override def isEnabled(level: log4j.Level, marker: Marker, message: String, params: AnyRef*): Boolean =
    enabled(level)

  override def isEnabled(level: log4j.Level, marker: Marker, message: String, p0: Any): Boolean =
    enabled(level)

  override def isEnabled(level: log4j.Level, marker: Marker, message: String, p0: Any, p1: Any): Boolean =
    enabled(level)

  override def isEnabled(level: log4j.Level, marker: Marker, message: String, p0: Any, p1: Any, p2: Any): Boolean =
    enabled(level)

  override def isEnabled(level: log4j.Level, marker: Marker, message: String, p0: Any, p1: Any, p2: Any, p3: Any): Boolean =
    enabled(level)

  override def isEnabled(level: log4j.Level, marker: Marker, message: String, p0: Any, p1: Any, p2: Any, p3: Any, p4: Any): Boolean =
    enabled(level)

  override def isEnabled(level: log4j.Level, marker: Marker, message: String, p0: Any, p1: Any, p2: Any, p3: Any, p4: Any, p5: Any): Boolean =
    enabled(level)

  override def isEnabled(level: log4j.Level, marker: Marker, message: String, p0: Any, p1: Any, p2: Any, p3: Any, p4: Any, p5: Any, p6: Any): Boolean =
    enabled(level)

  override def isEnabled(level: log4j.Level, marker: Marker, message: String, p0: Any, p1: Any, p2: Any, p3: Any, p4: Any, p5: Any, p6: Any, p7: Any): Boolean =
    enabled(level)

  override def isEnabled(level: log4j.Level, marker: Marker, message: String, p0: Any, p1: Any, p2: Any, p3: Any, p4: Any, p5: Any, p6: Any, p7: Any, p8: Any): Boolean =
    enabled(level)

  override def isEnabled(level: log4j.Level, marker: Marker, message: String, p0: Any, p1: Any, p2: Any, p3: Any, p4: Any, p5: Any, p6: Any, p7: Any, p8: Any, p9: Any): Boolean =
    enabled(level)

  override def logMessage(fqcn: String,
                          level: log4j.Level,
                          marker: Marker,
                          message: org.apache.logging.log4j.message.Message,
                          t: Throwable): Unit = logger.log(
    level = l2l(level).getOrElse(throw new RuntimeException(s"Unsupported level: $level")),
    message = message.getFormattedMessage,
    additionalMessages = Option(t).map(throwable2Message).toList
  )

  override def getLevel: log4j.Level = if (logger.includes(Level.Trace)) {
    log4j.Level.TRACE
  } else if (logger.includes(Level.Debug)) {
    log4j.Level.DEBUG
  } else if (logger.includes(Level.Info)) {
    log4j.Level.INFO
  } else if (logger.includes(Level.Warn)) {
    log4j.Level.WARN
  } else if (logger.includes(Level.Error)) {
    log4j.Level.ERROR
  } else {
    log4j.Level.OFF
  }
}