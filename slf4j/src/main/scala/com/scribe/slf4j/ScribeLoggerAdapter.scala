package com.scribe.slf4j

import org.slf4j.{Logger, Marker}
import org.slf4j.helpers.MarkerIgnoringBase
import org.slf4j.spi.LocationAwareLogger

import com.outr.scribe.{Logger => ScribeLogger, Platform, Level}

class ScribeLoggerAdapter(name: String) extends MarkerIgnoringBase with Logger {
  private lazy val logger = ScribeLogger(name)

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
    logger.warn(msg)
    logger.warn(Platform.throwable2String(t))
  }

  override def isErrorEnabled: Boolean = logger.accepts(Level.Error.value)

  override def isInfoEnabled: Boolean = logger.accepts(Level.Info.value)

  override def isDebugEnabled: Boolean = logger.accepts(Level.Debug.value)

  override def isTraceEnabled: Boolean = logger.accepts(Level.Trace.value)

  override def error(msg: String): Unit = logger.error(msg)

  override def error(format: String, arg: scala.Any): Unit = logger.error(format.format(arg))

  override def error(format: String, arg1: scala.Any, arg2: scala.Any): Unit = logger.error(format.format(arg1, arg2))

  override def error(format: String, arguments: AnyRef*): Unit = logger.error(format.format(arguments: _*))

  override def error(msg: String, t: Throwable): Unit = {
    logger.error(msg)
    logger.error(Platform.throwable2String(t))
  }

  override def debug(msg: String): Unit = logger.debug(msg)

  override def debug(format: String, arg: scala.Any): Unit = logger.debug(format.format(arg))

  override def debug(format: String, arg1: scala.Any, arg2: scala.Any): Unit = logger.debug(format.format(arg1, arg2))

  override def debug(format: String, arguments: AnyRef*): Unit = logger.debug(format.format(arguments: _*))

  override def debug(msg: String, t: Throwable): Unit = {
    logger.debug(msg)
    logger.debug(Platform.throwable2String(t))
  }

  override def isWarnEnabled: Boolean = logger.accepts(Level.Warn.value)

  override def trace(msg: String): Unit = logger.trace(msg)

  override def trace(format: String, arg: scala.Any): Unit = logger.trace(format.format(arg))

  override def trace(format: String, arg1: scala.Any, arg2: scala.Any): Unit = logger.trace(format.format(arg1, arg2))

  override def trace(format: String, arguments: AnyRef*): Unit = logger.trace(format.format(arguments: _*))

  override def trace(msg: String, t: Throwable): Unit = {
    logger.trace(msg)
    logger.trace(Platform.throwable2String(t))
  }

  override def info(msg: String): Unit = logger.info(msg)

  override def info(format: String, arg: scala.Any): Unit = logger.info(format.format(arg))

  override def info(format: String, arg1: scala.Any, arg2: scala.Any): Unit = logger.info(format.format(arg1, arg2))

  override def info(format: String, arguments: AnyRef*): Unit = logger.info(format.format(arguments: _*))

  override def info(msg: String, t: Throwable): Unit = {
    logger.info(msg)
    logger.info(Platform.throwable2String(t))
  }
}
