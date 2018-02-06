package scribe.slf4j

import org.slf4j.{Logger, Marker}
import org.slf4j.helpers.MarkerIgnoringBase
import org.slf4j.spi.LocationAwareLogger

import scribe.{Logger => ScribeLogger, Platform, Level}

class ScribeLoggerAdapter(name: String) extends MarkerIgnoringBase with Logger {
  private def logger = ScribeLogger.byName(name)

  ScribeLogger.update(name)(_.withClassNameOverride(name))

  def scribeLevel(level: Int): Level = level match {
    case LocationAwareLogger.TRACE_INT => Level.Trace
    case LocationAwareLogger.DEBUG_INT => Level.Debug
    case LocationAwareLogger.INFO_INT => Level.Info
    case LocationAwareLogger.WARN_INT => Level.Warn
    case LocationAwareLogger.ERROR_INT => Level.Error
  }

  override def warn(msg: String): Unit = logger.warn(msg)

  override def warn(format: String, arg: scala.Any): Unit = logger.warn(format.format(arg))

  override def warn(format: String, arguments: AnyRef*): Unit = logger.warn(format.format(arguments: _*))

  override def warn(format: String, arg1: scala.Any, arg2: scala.Any): Unit = logger.warn(format.format(arg1, arg2))

  override def warn(msg: String, t: Throwable): Unit = {
    if (msg != null) logger.warn(msg)
    if (t != null) logger.warn(t)
  }

  override def isErrorEnabled: Boolean = true //logger.accepts(Level.Error.value)

  override def isInfoEnabled: Boolean = true //logger.accepts(Level.Info.value)

  override def isDebugEnabled: Boolean = true //logger.accepts(Level.Debug.value)

  override def isTraceEnabled: Boolean = true //logger.accepts(Level.Trace.value)

  override def error(msg: String): Unit = logger.error(msg)

  override def error(format: String, arg: scala.Any): Unit = logger.error(format.format(arg))

  override def error(format: String, arg1: scala.Any, arg2: scala.Any): Unit = logger.error(format.format(arg1, arg2))

  override def error(format: String, arguments: AnyRef*): Unit = logger.error(format.format(arguments: _*))

  override def error(msg: String, t: Throwable): Unit = {
    if (msg != null) logger.error(msg)
    if (t != null) logger.error(t)
  }

  override def debug(msg: String): Unit = logger.debug(msg)

  override def debug(format: String, arg: scala.Any): Unit = logger.debug(format.format(arg))

  override def debug(format: String, arg1: scala.Any, arg2: scala.Any): Unit = logger.debug(format.format(arg1, arg2))

  override def debug(format: String, arguments: AnyRef*): Unit = logger.debug(format.format(arguments: _*))

  override def debug(msg: String, t: Throwable): Unit = {
    if (msg != null) logger.debug(msg)
    if (t != null) logger.debug(t)
  }

  override def isWarnEnabled: Boolean = true //logger.accepts(Level.Warn.value)

  override def trace(msg: String): Unit = logger.trace(msg)

  override def trace(format: String, arg: scala.Any): Unit = logger.trace(format.format(arg))

  override def trace(format: String, arg1: scala.Any, arg2: scala.Any): Unit = logger.trace(format.format(arg1, arg2))

  override def trace(format: String, arguments: AnyRef*): Unit = logger.trace(format.format(arguments: _*))

  override def trace(msg: String, t: Throwable): Unit = {
    if (msg != null) logger.trace(msg)
    if (t != null) logger.trace(t)
  }

  override def info(msg: String): Unit = logger.info(msg)

  override def info(format: String, arg: scala.Any): Unit = logger.info(format.format(arg))

  override def info(format: String, arg1: scala.Any, arg2: scala.Any): Unit = logger.info(format.format(arg1, arg2))

  override def info(format: String, arguments: AnyRef*): Unit = logger.info(format.format(arguments: _*))

  override def info(msg: String, t: Throwable): Unit = {
    if (msg != null) logger.info(msg)
    if (t != null) logger.info(t)
  }
}
