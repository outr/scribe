package scribe

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
                  multiplier: Double = 1.0) extends LoggerSupport {
  override def name: Option[String] = Logger.name(this)

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

  def name(logger: Logger): Option[String] = loggers.collectFirst {
    case (key, value) if value eq logger => key
  }

  def assign(name: String, logger: Logger): Unit = synchronized {
    loggers += name -> logger
  }

  def replace(oldLogger: Logger, newLogger: Logger): Unit = {
    name(oldLogger).foreach { key =>
      assign(key, newLogger)
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