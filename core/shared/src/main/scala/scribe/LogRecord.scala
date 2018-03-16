package scribe

import scala.annotation.tailrec

trait LogRecord {
  def level: Level
  def value: Double
  def messageValue: Any
  def stringify: Any => String
  def className: String
  def methodName: Option[String]
  def lineNumber: Option[Int]
  def thread: Thread
  def timeStamp: Long

  def message: String

  def boost(booster: Double => Double): LogRecord = copy(value = booster(value))

  def copy(level: Level = level,
           value: Double = value,
           message: Any = messageValue,
           stringify: Any => String = stringify,
           className: String = className,
           methodName: Option[String] = methodName,
           lineNumber: Option[Int] = lineNumber,
           thread: Thread = thread,
           timeStamp: Long = timeStamp): LogRecord

  def dispose(): Unit
}

object LogRecord {
  object Stringify {
    val Default: Any => String = {
      case t: Throwable => throwable2String(t)
      case v => String.valueOf(v)
    }
  }

  def apply(level: Level,
            value: Double,
            message: Any,
            stringify: Any => String,
            className: String,
            methodName: Option[String],
            lineNumber: Option[Int],
            thread: Thread = Thread.currentThread(),
            timeStamp: Long = System.currentTimeMillis()): LogRecord = {
    SimpleLogRecord(level, value, message, stringify, className, methodName, lineNumber, thread, timeStamp)
  }

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

  case class SimpleLogRecord(level: Level,
                             value: Double,
                             messageValue: Any,
                             stringify: Any => String,
                             className: String,
                             methodName: Option[String],
                             lineNumber: Option[Int],
                             thread: Thread,
                             timeStamp: Long) extends LogRecord {
    override lazy val message: String = stringify(messageValue)

    def copy(level: Level = level,
             value: Double = value,
             message: Any = messageValue,
             stringify: Any => String,
             className: String = className,
             methodName: Option[String] = methodName,
             lineNumber: Option[Int] = lineNumber,
             thread: Thread = thread,
             timeStamp: Long = timeStamp): LogRecord = {
      SimpleLogRecord(level, value, message, stringify, className, methodName, lineNumber, thread, timeStamp)
    }

    override def dispose(): Unit = {}
  }
}