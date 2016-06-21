package com.outr.scribe

import scala.annotation.tailrec

object Platform {
  private val NativeMethod = -2

  val LineSeparator = System.getProperty("line.separator")

  def formatDate(pattern: String, timestamp: Long): String =
    pattern.format(timestamp)

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
        if (head.getLineNumber == NativeMethod) {
          b.append("Native Method")
        } else {
          b.append(head.getFileName)
          if (head.getLineNumber > 0) {
            b.append(':')
            b.append(head.getLineNumber)
          }
        }
        b.append(')')
        b.append(LineSeparator)
        writeStackTrace(b, elements.tail)
      }
    }
  }
}