package scribe

import scribe.data.MDC
import scribe.message.LoggableMessage

import scala.language.experimental.macros

trait LoggerSupport[F] extends Any {
  def log(record: LogRecord): F

  def log(level: Level, mdc: MDC, messages: LoggableMessage*)
         (implicit pkg: sourcecode.Pkg,
          fileName: sourcecode.FileName,
          name: sourcecode.Name,
          line: sourcecode.Line): F = {
    log(LoggerSupport(level, messages.toList, pkg, fileName, name, line, mdc))
  }

  def trace(messages: LoggableMessage*)(implicit pkg: sourcecode.Pkg,
                                        fileName: sourcecode.FileName,
                                        name: sourcecode.Name,
                                        line: sourcecode.Line,
                                        mdc: MDC): F = log(Level.Trace, mdc, messages: _*)

  def debug(messages: LoggableMessage*)(implicit pkg: sourcecode.Pkg,
                                        fileName: sourcecode.FileName,
                                        name: sourcecode.Name,
                                        line: sourcecode.Line,
                                        mdc: MDC): F = log(Level.Debug, mdc, messages: _*)

  def info(messages: LoggableMessage*)(implicit pkg: sourcecode.Pkg,
                                       fileName: sourcecode.FileName,
                                       name: sourcecode.Name,
                                       line: sourcecode.Line,
                                       mdc: MDC): F = log(Level.Info, mdc, messages: _*)

  def warn(messages: LoggableMessage*)(implicit pkg: sourcecode.Pkg,
                                       fileName: sourcecode.FileName,
                                       name: sourcecode.Name,
                                       line: sourcecode.Line,
                                       mdc: MDC): F = log(Level.Warn, mdc, messages: _*)

  def error(messages: LoggableMessage*)(implicit pkg: sourcecode.Pkg,
                                        fileName: sourcecode.FileName,
                                        name: sourcecode.Name,
                                        line: sourcecode.Line,
                                        mdc: MDC): F = log(Level.Error, mdc, messages: _*)

  /**
   * Includes MDC elapsed to show elapsed time within the block
   *
   * @param f the code block to time
   */
  def elapsed[Return](f: => Return)(implicit mdc: MDC): Return = {
    val key = "elapsed"
    val exists = mdc.contains(key)
    if (!exists) mdc.elapsed(key)
    try {
      f
    } finally {
      if (!exists) mdc.remove(key)
    }
  }

  /**
   * Contextualize key/value pairs set on MDC. This will be made avoid on each log record within
   *
   * @param keyValues tuples of key/value pairs to set on MDC
   * @param f         the context for which these MDC values are set
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

  def apply(level: Level,
            messages: List[LoggableMessage],
            pkg: sourcecode.Pkg,
            fileName: sourcecode.FileName,
            name: sourcecode.Name,
            line: sourcecode.Line,
            mdc: MDC): LogRecord = {
    val (fn, className) = LoggerSupport.className(pkg, fileName)
    val methodName = name.value match {
      case "anonymous" | "" => None
      case v => Option(v)
    }
    LogRecord(
      level = level,
      value = level.value,
      messages = messages,
      fileName = fn,
      className = className,
      methodName = methodName,
      line = Some(line.value),
      column = None,
      data = mdc.map
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