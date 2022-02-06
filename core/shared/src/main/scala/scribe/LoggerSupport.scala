package scribe

import scribe.data.MDC
import scribe.message.{LoggableMessage, Message}

import scala.language.experimental.macros

trait LoggerSupport[F] extends Any {
  def log[M](record: LogRecord[M]): F

  def log[M: Loggable](level: Level, message: => M, additionalMessages: List[LoggableMessage] = Nil)
                      (implicit pkg: sourcecode.Pkg,
                       fileName: sourcecode.FileName,
                       name: sourcecode.Name,
                       line: sourcecode.Line): F = {
    log[M](LoggerSupport[M](level, message, additionalMessages, pkg, fileName, name, line))
  }

  def trace()(implicit pkg: sourcecode.Pkg,
              fileName: sourcecode.FileName,
              name: sourcecode.Name,
              line: sourcecode.Line): F = log[String](Level.Trace, "", Nil)
  def debug()(implicit pkg: sourcecode.Pkg,
              fileName: sourcecode.FileName,
              name: sourcecode.Name,
              line: sourcecode.Line): F = log[String](Level.Debug, "", Nil)
  def info()(implicit pkg: sourcecode.Pkg,
             fileName: sourcecode.FileName,
             name: sourcecode.Name,
             line: sourcecode.Line): F = log[String](Level.Info, "", Nil)
  def warn()(implicit pkg: sourcecode.Pkg,
             fileName: sourcecode.FileName,
             name: sourcecode.Name,
             line: sourcecode.Line): F = log[String](Level.Warn, "", Nil)
  def error()(implicit pkg: sourcecode.Pkg,
              fileName: sourcecode.FileName,
              name: sourcecode.Name,
              line: sourcecode.Line): F = log[String](Level.Error, "", Nil)

  def trace[M : Loggable](message: => M, additionalMessages: LoggableMessage*)
                         (implicit pkg: sourcecode.Pkg,
                          fileName: sourcecode.FileName,
                          name: sourcecode.Name,
                          line: sourcecode.Line): F = log[M](Level.Trace, message, additionalMessages.toList)
  def debug[M : Loggable](message: => M, additionalMessages: LoggableMessage*)
                         (implicit pkg: sourcecode.Pkg,
                          fileName: sourcecode.FileName,
                          name: sourcecode.Name,
                          line: sourcecode.Line): F = log[M](Level.Debug, message, additionalMessages.toList)
  def info[M : Loggable](message: => M, additionalMessages: LoggableMessage*)
                        (implicit pkg: sourcecode.Pkg,
                         fileName: sourcecode.FileName,
                         name: sourcecode.Name,
                         line: sourcecode.Line): F = log[M](Level.Info, message, additionalMessages.toList)
  def warn[M : Loggable](message: => M, additionalMessages: LoggableMessage*)
                        (implicit pkg: sourcecode.Pkg,
                         fileName: sourcecode.FileName,
                         name: sourcecode.Name,
                         line: sourcecode.Line): F = log[M](Level.Warn, message, additionalMessages.toList)
  def error[M : Loggable](message: => M, additionalMessages: LoggableMessage*)
                         (implicit pkg: sourcecode.Pkg,
                          fileName: sourcecode.FileName,
                          name: sourcecode.Name,
                          line: sourcecode.Line): F = log[M](Level.Error, message, additionalMessages.toList)

  /**
    * Includes MDC elapsed to show elapsed time within the block
    *
    * @param f the code block to time
    */
  def elapsed[Return](f: => Return): Return = {
    val key = "elapsed"
    val exists = MDC.contains(key)
    if (!exists) MDC.elapsed(key)
    try {
      f
    } finally {
      if (!exists) MDC.remove(key)
    }
  }

  /**
    * Contextualize key/value pairs set on MDC. This will be made avoid on each log record within
    *
    * @param keyValues tuples of key/value pairs to set on MDC
    * @param f the context for which these MDC values are set
    */
  def apply[Return](keyValues: (String, Any)*)(f: => Return): Return = {
    keyValues.foreach {
      case (key, value) => MDC.update(key, value)
    }
    try {
      f
    } finally {
      keyValues.foreach(t => MDC.remove(t._1))
    }
  }
}

object LoggerSupport {
  private var map = Map.empty[sourcecode.Pkg, Map[sourcecode.FileName, (String, String)]]

  def apply[M](level: Level,
               message: => M,
               additionalMessages: List[LoggableMessage],
               pkg: sourcecode.Pkg,
               fileName: sourcecode.FileName,
               name: sourcecode.Name,
               line: sourcecode.Line)
              (implicit loggable: Loggable[M]): LogRecord[M] = {
    val (fn, className) = LoggerSupport.className(pkg, fileName)
    val methodName = name.value match {
      case "anonymous" | "" => None
      case v => Option(v)
    }
    LogRecord(
      level = level,
      value = level.value,
      message = Message(message),
      additionalMessages = additionalMessages,
      fileName = fn,
      className = className,
      methodName = methodName,
      line = Some(line.value),
      column = None
    )
  }

  def className(pkg: sourcecode.Pkg, fileName: sourcecode.FileName): (String, String) = map.get(pkg) match {
    case Some(m) => m.get(fileName) match {
      case Some(value) => value
      case None =>
        val value = generateClassName(pkg, fileName)
        LoggerSupport.synchronized {
          map += pkg -> (m + (fileName -> value))
        }
        value
    }
    case None =>
      val value = generateClassName(pkg, fileName)
      LoggerSupport.synchronized {
        map += pkg -> Map(fileName -> value)
      }
      value
  }

  private def generateClassName(pkg: sourcecode.Pkg, fileName: sourcecode.FileName): (String, String) = {
    val backSlash = fileName.value.lastIndexOf('\\')
    val fn = if (backSlash != -1) {
      fileName.value.substring(backSlash + 1)
    } else {
      fileName.value
    }
    fn -> s"${pkg.value}.${fn.substring(0, fn.length - 6)}"
  }
}