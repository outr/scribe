package scribe.jul

import scribe.Loggable.StringLoggable
import scribe.message.Message
import scribe._

import java.util.logging.{Level => JLevel}

object JULHandler extends java.util.logging.Handler {
  override def publish(record: java.util.logging.LogRecord): Unit = {
    val logger = scribe.Logger(record.getLoggerName)
    val level = l2l(record.getLevel)
    val logRecord = LogRecord(
      level = level,
      value = level.value,
      message = Message(record.getMessage),
      additionalMessages = Option(record.getThrown).map(throwable2Message).toList,
      fileName = "",
      className = Option(record.getSourceClassName).getOrElse(record.getLoggerName),
      methodName = Option(record.getSourceMethodName),
      line = None,
      column = None
    )
    logger.log(logRecord)
  }

  private def l2l(level: JLevel): Level = level match {
    case JLevel.FINEST => Level.Trace
    case JLevel.FINER => Level.Trace
    case JLevel.FINE => Level.Trace
    case JLevel.CONFIG => Level.Debug
    case JLevel.INFO => Level.Info
    case JLevel.WARNING => Level.Warn
    case JLevel.SEVERE => Level.Error
    case JLevel.OFF => Level.Trace
    case JLevel.ALL => Level.Fatal
  }

  override def flush(): Unit = {}

  override def close(): Unit = {}
}
