package scribe

import scala.annotation.tailrec
import perfolation._

trait LogRecord[M] {
  def level: Level
  def value: Double
  def messageValue: M
  def stringify: Loggable[M]
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
           message: M = messageValue,
           stringify: Loggable[M] = stringify,
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
  def apply[T](level: Level,
               value: Double,
               message: T,
               stringify: Loggable[T],
               throwable: Option[Throwable],
               fileName: String,
               className: String,
               methodName: Option[String],
               lineNumber: Option[Int],
               thread: Thread = Thread.currentThread(),
               timeStamp: Long = System.currentTimeMillis()): LogRecord[T] = {
    SimpleLogRecord(level, value, message, stringify, throwable, fileName, className, methodName, lineNumber, thread, timeStamp)
  }

  def simple(message: String,
             fileName: String,
             className: String,
             methodName: Option[String] = None,
             lineNumber: Option[Int] = None,
             level: Level = Level.Info,
             thread: Thread = Thread.currentThread(),
             timeStamp: Long = System.currentTimeMillis()): LogRecord[String] = {
    apply[String](level, level.value, message, Loggable.StringLoggable, None, fileName, className, methodName, lineNumber, thread, timeStamp)
  }

  /**
    * Converts a Throwable to a String representation for output in logging.
    */
  @tailrec
  final def throwable2String(message: Option[String],
                             t: Throwable,
                             primaryCause: Boolean = true,
                             b: StringBuilder = new StringBuilder): String = {
    message.foreach(m => b.append(p"$m${Platform.lineSeparator}"))
    if (!primaryCause) {
      b.append("Caused by: ")
    }
    b.append(t.getClass.getName)
    if (Option(t.getLocalizedMessage).nonEmpty) {
      b.append(": ")
      b.append(t.getLocalizedMessage)
    }
    b.append(Platform.lineSeparator)
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
        b.append(Platform.lineSeparator)
        writeStackTrace(b, elements.tail)
      }
    }
  }

  case class SimpleLogRecord[T](level: Level,
                                value: Double,
                                messageValue: T,
                                stringify: Loggable[T],
                                throwable: Option[Throwable],
                                fileName: String,
                                className: String,
                                methodName: Option[String],
                                lineNumber: Option[Int],
                                thread: Thread,
                                timeStamp: Long) extends LogRecord[T] {
    override lazy val message: String = {
      val msg = stringify(messageValue)
      throwable match {
        case Some(t) => throwable2String(Option(msg), t)
        case None => stringify(messageValue)
      }
    }

    def copy(level: Level = level,
             value: Double = value,
             message: T = messageValue,
             stringify: Loggable[T],
             throwable: Option[Throwable],
             fileName: String = fileName,
             className: String = className,
             methodName: Option[String] = methodName,
             lineNumber: Option[Int] = lineNumber,
             thread: Thread = thread,
             timeStamp: Long = timeStamp): LogRecord[T] = {
      SimpleLogRecord(level, value, message, stringify, throwable, fileName, className, methodName, lineNumber, thread, timeStamp)
    }

    override def dispose(): Unit = {}
  }
}
