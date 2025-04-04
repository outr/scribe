package scribe

import scribe.mdc.MDC
import scribe.message.LoggableMessage

import scala.language.experimental.macros

trait LoggerSupport[F] extends Any {
  def log(record: => LogRecord): F

  def log(level: Level, mdc: MDC, features: LogFeature*)
         (implicit pkg: sourcecode.Pkg,
          fileName: sourcecode.FileName,
          name: sourcecode.Name,
          line: sourcecode.Line): F = {
    val r = LoggerSupport(level, Nil, pkg, fileName, name, line, mdc)
    val record = features.foldLeft(r)((record, feature) => feature(record))
    log(record)
  }

  def trace(message: => String)(implicit pkg: sourcecode.Pkg,
                                fileName: sourcecode.FileName,
                                name: sourcecode.Name,
                                line: sourcecode.Line,
                                mdc: MDC): F = log(Level.Trace, mdc, message)

  def trace(features: LogFeature*)(implicit pkg: sourcecode.Pkg,
                                   fileName: sourcecode.FileName,
                                   name: sourcecode.Name,
                                   line: sourcecode.Line,
                                   mdc: MDC): F = log(Level.Trace, mdc, features: _*)

  def debug(message: => String)(implicit pkg: sourcecode.Pkg,
                                fileName: sourcecode.FileName,
                                name: sourcecode.Name,
                                line: sourcecode.Line,
                                mdc: MDC): F = log(Level.Debug, mdc, message)

  def debug(features: LogFeature*)(implicit pkg: sourcecode.Pkg,
                                   fileName: sourcecode.FileName,
                                   name: sourcecode.Name,
                                   line: sourcecode.Line,
                                   mdc: MDC): F = log(Level.Debug, mdc, features: _*)

  def info(message: => String)(implicit pkg: sourcecode.Pkg,
                               fileName: sourcecode.FileName,
                               name: sourcecode.Name,
                               line: sourcecode.Line,
                               mdc: MDC): F = log(Level.Info, mdc, message)

  def info(features: LogFeature*)(implicit pkg: sourcecode.Pkg,
                                  fileName: sourcecode.FileName,
                                  name: sourcecode.Name,
                                  line: sourcecode.Line,
                                  mdc: MDC): F = log(Level.Info, mdc, features: _*)

  def warn(message: => String)(implicit pkg: sourcecode.Pkg,
                               fileName: sourcecode.FileName,
                               name: sourcecode.Name,
                               line: sourcecode.Line,
                               mdc: MDC): F = log(Level.Warn, mdc, message)

  def warn(features: LogFeature*)(implicit pkg: sourcecode.Pkg,
                                  fileName: sourcecode.FileName,
                                  name: sourcecode.Name,
                                  line: sourcecode.Line,
                                  mdc: MDC): F = log(Level.Warn, mdc, features: _*)

  def error(message: => String)(implicit pkg: sourcecode.Pkg,
                                fileName: sourcecode.FileName,
                                name: sourcecode.Name,
                                line: sourcecode.Line,
                                mdc: MDC): F = log(Level.Error, mdc, message)

  def error(features: LogFeature*)(implicit pkg: sourcecode.Pkg,
                                   fileName: sourcecode.FileName,
                                   name: sourcecode.Name,
                                   line: sourcecode.Line,
                                   mdc: MDC): F = log(Level.Error, mdc, features: _*)

  def fatal(message: => String)(implicit pkg: sourcecode.Pkg,
                                fileName: sourcecode.FileName,
                                name: sourcecode.Name,
                                line: sourcecode.Line,
                                mdc: MDC): F = log(Level.Fatal, mdc, message)

  def fatal(features: LogFeature*)(implicit pkg: sourcecode.Pkg,
                                   fileName: sourcecode.FileName,
                                   name: sourcecode.Name,
                                   line: sourcecode.Line,
                                   mdc: MDC): F = log(Level.Fatal, mdc, features: _*)

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
      levelValue = level.value,
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