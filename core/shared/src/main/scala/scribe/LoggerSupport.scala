package scribe

import scala.language.experimental.macros

trait LoggerSupport {
  def log[M](record: LogRecord[M]): Unit

  def log[M: Loggable](level: Level, message: => M, throwable: Option[Throwable])
                      (implicit pkg: sourcecode.Pkg,
                       fileName: sourcecode.FileName,
                       name: sourcecode.Name,
                       line: sourcecode.Line): Unit = {
    val backSlash = fileName.value.lastIndexOf('\\')
    val fn = if (backSlash != -1) {
      fileName.value.substring(backSlash + 1)
    } else {
      fileName.value
    }
    val className = s"${pkg.value}.${fn.substring(0, fn.length - 6)}"
    val methodName = name.value match {
      case "anonymous" | "" => None
      case v => Option(v)
    }
    log[M](LogRecord(
      level = level,
      value = level.value,
      message = new LazyMessage[M](() => message),
      loggable = implicitly[Loggable[M]],
      throwable = throwable,
      fileName = fileName.value,
      className = className,
      methodName = methodName,
      line = Some(line.value),
      column = None
    ))
  }

  def trace()(implicit pkg: sourcecode.Pkg,
              fileName: sourcecode.FileName,
              name: sourcecode.Name,
              line: sourcecode.Line): Unit = log[String](Level.Trace, "", None)
  def debug()(implicit pkg: sourcecode.Pkg,
              fileName: sourcecode.FileName,
              name: sourcecode.Name,
              line: sourcecode.Line): Unit = log[String](Level.Debug, "", None)
  def info()(implicit pkg: sourcecode.Pkg,
             fileName: sourcecode.FileName,
             name: sourcecode.Name,
             line: sourcecode.Line): Unit = log[String](Level.Info, "", None)
  def warn()(implicit pkg: sourcecode.Pkg,
             fileName: sourcecode.FileName,
             name: sourcecode.Name,
             line: sourcecode.Line): Unit = log[String](Level.Warn, "", None)
  def error()(implicit pkg: sourcecode.Pkg,
              fileName: sourcecode.FileName,
              name: sourcecode.Name,
              line: sourcecode.Line): Unit = log[String](Level.Error, "", None)

  def trace[M: Loggable](message: => M)
                        (implicit pkg: sourcecode.Pkg,
                         fileName: sourcecode.FileName,
                         name: sourcecode.Name,
                         line: sourcecode.Line): Unit = log[M](Level.Trace, message, None)
  def debug[M: Loggable](message: => M)
                        (implicit pkg: sourcecode.Pkg,
                         fileName: sourcecode.FileName,
                         name: sourcecode.Name,
                         line: sourcecode.Line): Unit = log[M](Level.Debug, message, None)
  def info[M: Loggable](message: => M)
                       (implicit pkg: sourcecode.Pkg,
                        fileName: sourcecode.FileName,
                        name: sourcecode.Name,
                        line: sourcecode.Line): Unit = log[M](Level.Info, message, None)
  def warn[M: Loggable](message: => M)
                       (implicit pkg: sourcecode.Pkg,
                        fileName: sourcecode.FileName,
                        name: sourcecode.Name,
                        line: sourcecode.Line): Unit = log[M](Level.Warn, message, None)
  def error[M: Loggable](message: => M)
                        (implicit pkg: sourcecode.Pkg,
                         fileName: sourcecode.FileName,
                         name: sourcecode.Name,
                         line: sourcecode.Line): Unit = log[M](Level.Error, message, None)

  def trace[M : Loggable](message: => M, t: Throwable)
                         (implicit pkg: sourcecode.Pkg,
                          fileName: sourcecode.FileName,
                          name: sourcecode.Name,
                          line: sourcecode.Line): Unit = log[M](Level.Trace, message, Some(t))
  def debug[M : Loggable](message: => M, t: Throwable)
                         (implicit pkg: sourcecode.Pkg,
                          fileName: sourcecode.FileName,
                          name: sourcecode.Name,
                          line: sourcecode.Line): Unit = log[M](Level.Debug, message, Some(t))
  def info[M : Loggable](message: => M, t: Throwable)
                        (implicit pkg: sourcecode.Pkg,
                         fileName: sourcecode.FileName,
                         name: sourcecode.Name,
                         line: sourcecode.Line): Unit = log[M](Level.Info, message, Some(t))
  def warn[M : Loggable](message: => M, t: Throwable)
                        (implicit pkg: sourcecode.Pkg,
                         fileName: sourcecode.FileName,
                         name: sourcecode.Name,
                         line: sourcecode.Line): Unit = log[M](Level.Warn, message, Some(t))
  def error[M : Loggable](message: => M, t: Throwable)
                         (implicit pkg: sourcecode.Pkg,
                          fileName: sourcecode.FileName,
                          name: sourcecode.Name,
                          line: sourcecode.Line): Unit = log[M](Level.Error, message, Some(t))

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