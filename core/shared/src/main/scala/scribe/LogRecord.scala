package scribe

import scala.annotation.tailrec
import perfolation._
import scribe.util.Time

trait LogRecord[M] {
  def level: Level
  def value: Double
  def messageFunction: () => M
  def loggable: Loggable[M]
  def throwable: Option[Throwable]
  def fileName: String
  def className: String
  def methodName: Option[String]
  def lineNumber: Option[Int]
  def thread: Thread
  def timeStamp: Long

  def message: String

  def boost(booster: Double => Double): LogRecord[M] = copy(value = booster(value))

  def copy(level: Level = level,
           value: Double = value,
           messageFunction: () => M = messageFunction,
           loggable: Loggable[M] = loggable,
           throwable: Option[Throwable] = throwable,
           fileName: String = fileName,
           className: String = className,
           methodName: Option[String] = methodName,
           lineNumber: Option[Int] = lineNumber,
           thread: Thread = thread,
           timeStamp: Long = timeStamp): LogRecord[M]

  def dispose(): Unit
}

object LogRecord {
  def apply[M](level: Level,
               value: Double,
               messageFunction: () => M,
               loggable: Loggable[M],
               throwable: Option[Throwable],
               fileName: String,
               className: String,
               methodName: Option[String],
               lineNumber: Option[Int],
               thread: Thread = Thread.currentThread(),
               timeStamp: Long = Time()): LogRecord[M] = {
    SimpleLogRecord(level, value, messageFunction, loggable, throwable, fileName, className, methodName, lineNumber, thread, timeStamp)
  }

  def simple(messageFunction: () => String,
             fileName: String,
             className: String,
             methodName: Option[String] = None,
             lineNumber: Option[Int] = None,
             level: Level = Level.Info,
             thread: Thread = Thread.currentThread(),
             timeStamp: Long = Time()): LogRecord[String] = {
    apply[String](level, level.value, messageFunction, Loggable.StringLoggable, None, fileName, className, methodName, lineNumber, thread, timeStamp)
  }

  /**
    * Converts a Throwable to a String representation for output in logging.
    */
  @tailrec
  final def throwable2String(message: Option[String],
                             t: Throwable,
                             primaryCause: Boolean = true,
                             b: StringBuilder = new StringBuilder): String = {
    message.foreach(m => b.append(p"$m${scribe.lineSeparator}"))
    if (!primaryCause) {
      b.append("Caused by: ")
    }
    b.append(t.getClass.getName)
    if (Option(t.getLocalizedMessage).nonEmpty) {
      b.append(": ")
      b.append(t.getLocalizedMessage)
    }
    b.append(scribe.lineSeparator)
    writeStackTrace(b, t.getStackTrace)
    if (Option(t.getCause).isEmpty) {
      b.toString()
    } else {
      throwable2String(None, t.getCause, primaryCause = false, b = b)
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
        if (head.getLineNumber == -2) {
          b.append("Native Method")
        } else {
          b.append(head.getFileName)
          if (head.getLineNumber > 0) {
            b.append(':')
            b.append(head.getLineNumber)
          }
        }
        b.append(')')
        b.append(scribe.lineSeparator)
        writeStackTrace(b, elements.tail)
      }
    }
  }

  case class SimpleLogRecord[M](level: Level,
                                value: Double,
                                messageFunction: () => M,
                                loggable: Loggable[M],
                                throwable: Option[Throwable],
                                fileName: String,
                                className: String,
                                methodName: Option[String],
                                lineNumber: Option[Int],
                                thread: Thread,
                                timeStamp: Long) extends LogRecord[M] {
    override lazy val message: String = {
      val msg = loggable(messageFunction())
      throwable match {
        case Some(t) => throwable2String(Option(msg), t)
        case None => loggable(messageFunction())
      }
    }

    def copy(level: Level = level,
             value: Double = value,
             messageFunction: () => M = messageFunction,
             loggable: Loggable[M],
             throwable: Option[Throwable],
             fileName: String = fileName,
             className: String = className,
             methodName: Option[String] = methodName,
             lineNumber: Option[Int] = lineNumber,
             thread: Thread = thread,
             timeStamp: Long = timeStamp): LogRecord[M] = {
      SimpleLogRecord(level, value, messageFunction, loggable, throwable, fileName, className, methodName, lineNumber, thread, timeStamp)
    }

    override def dispose(): Unit = {}
  }
}
