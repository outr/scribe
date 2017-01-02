package com.outr.scribe

import java.io.PrintStream

import scala.annotation.tailrec
import scala.language.experimental.macros

/**
  * Logger is the class to which all logging calls are made. The primary use-case of Logger is via use of the Logging
  * trait that may be mixed-in to any class.
  *
  * @param parentName the name of the parent logger if there is one (defaults to the root logger)
  * @param multiplier the multiplier that should be applied to boost the value of all messages routed through this
  *                   logger (Defaults to 1.0)
  */
case class Logger(parentName: Option[String] = Some(Logger.rootName),
                  multiplier: Double = 1.0) {
  private[scribe] var handlers = Set.empty[LogHandler]

  def name: Option[String] = Logger.name(this)
  def parent: Option[Logger] = parentName.map(Logger.byName)

  /**
    * Trace log entry. Uses Macros to optimize performance.
    */
  def trace(message: => Any): Unit = macro Macros.trace

  def trace(t: => Throwable): Unit = macro Macros.traceThrowable

  /**
    * Debug log entry. Uses Macros to optimize performance.
    */
  def debug(message: => Any): Unit = macro Macros.debug

  def debug(t: => Throwable): Unit = macro Macros.debugThrowable

  /**
    * Info log entry. Uses Macros to optimize performance.
    */
  def info(message: => Any): Unit = macro Macros.info

  def info(t: => Throwable): Unit = macro Macros.infoThrowable

  /**
    * Warn log entry. Uses Macros to optimize performance.
    */
  def warn(message: => Any): Unit = macro Macros.warn

  def warn(t: => Throwable): Unit = macro Macros.warnThrowable

  /**
    * Error log entry. Uses Macros to optimize performance.
    */
  def error(message: => Any): Unit = macro Macros.error

  /**
    * Error log entry. Uses Macros to optimize performance.
    */
  def error(t: => Throwable): Unit = macro Macros.errorThrowable

  /**
    * Log method invoked by trace, debug, info, warn, and error. Ideally should not be called directly as it will not
    * be able to take advantage of Macro optimizations.
    *
    * @param level the logging level
    * @param message function to derive the message for the log
    * @param methodName the method name if applicable
    * @param lineNumber the line number the logging was invoked on
    */
  def log(level: Level,
          message: => Any,
          className: String,
          methodName: Option[String] = None,
          lineNumber: Int = -1): Unit = if (accepts(level.value)) {
    val record = LogRecord(level, level.value * multiplier, () => message, className, methodName, lineNumber)
    log(record)
  }

  protected[scribe] def log(record: LogRecord): Unit = {
    handlers.foreach(h => h.log(record))
    parent.foreach(p => p.log(record.updateValue(record.value * p.multiplier)))
  }

  /**
    * Returns true if the supplied value will be accepted by a handler of this logger or an ancestor (up the parent
    * tree)
    */
  def accepts(value: Double): Boolean = {
    val v = value * multiplier

    handlers.exists(handler => handler.accepts(v)) || parent.exists(p => p.accepts(v))
  }

  /**
    * Adds a handler that will receive log records submitted to this logger and any descendant loggers.
    */
  def addHandler(handler: LogHandler): Unit = synchronized {
    handlers += handler
  }

  /**
    * Removes an handler that was previously added to this logger.
    */
  def removeHandler(handler: LogHandler): Unit = synchronized {
    handlers -= handler
  }

  /**
    * Removes all handlers currently on this logger.
    */
  def clearHandlers(): Unit = synchronized {
    handlers = Set.empty
  }

  /**
    * Replaces the current logger with what is returned by `updater`. Existing handlers are added to the new Logger.
    *
    * @param updater function to create the new logger
    */
  def update(updater: => Logger): Unit = {
    val updated: Logger = updater
    if (handlers.nonEmpty) {
      updated.handlers ++= handlers
    }
    Logger.replace(this, updated)
  }
}

object Logger {
  private var loggers = Map.empty[String, Logger]
  private val nativeMethod = -2

  val systemOut: PrintStream = System.out
  val systemErr: PrintStream = System.err

  def byName(name: String): Logger = synchronized {
    loggers.get(name) match {
      case Some(l) => l
      case None => {
        val l = Logger()
        loggers += name -> l
        l
      }
    }
  }

  def name(logger: Logger): Option[String] = loggers.find(_._2 eq logger).map(_._1)

  def set(name: String, logger: Logger): Unit = synchronized {
    loggers += name -> logger
  }

  def replace(oldLogger: Logger, newLogger: Logger): Unit = {
    name(oldLogger).foreach { key =>
      set(key, newLogger)
    }
  }

  def clear(name: String): Unit = synchronized {
    loggers -= name
  }

  val rootName: String = "root"

  /**
    * The root logger is the default parent of all loggers and comes default with a default LogHandler added.
    */
  def root: Logger = byName(rootName)

  // Initial setup of root logger
  root.update {
    root.copy(parentName = None)
  }
  root.addHandler(LogHandler())

  /**
    * Converts a Throwable to a String representation for output in logging.
    */
  @tailrec
  final def throwable2String(t: Throwable,
                             primaryCause: Boolean = true,
                             b: StringBuilder = new StringBuilder): String = {
    if (!primaryCause) {
      b.append("Caused by: ")
    }
    b.append(t.getClass.getName)
    if (Option(t.getLocalizedMessage).nonEmpty) {
      b.append(": ")
      b.append(t.getLocalizedMessage)
    }
    b.append(System.getProperty("line.separator"))
    writeStackTrace(b, t.getStackTrace)
    if (Option(t.getCause).isEmpty) {
      b.toString()
    } else {
      throwable2String(t.getCause, primaryCause = false, b = b)
    }
  }

  @tailrec
  private def writeStackTrace(b: StringBuilder, elements: Array[StackTraceElement]): Unit = {
    elements.headOption match {
      case None => // No more elements
      case Some(head) => {
        b.append("\tat ")
        b.append(head.getClassName)
        b.append('.')
        b.append(head.getMethodName)
        b.append('(')
        if (head.getLineNumber == nativeMethod) {
          b.append("Native Method")
        } else {
          b.append(head.getFileName)
          if (head.getLineNumber > 0) {
            b.append(':')
            b.append(head.getLineNumber)
          }
        }
        b.append(')')
        b.append(Platform.lineSeparator)
        writeStackTrace(b, elements.tail)
      }
    }
  }
}