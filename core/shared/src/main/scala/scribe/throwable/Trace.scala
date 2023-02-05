package scribe.throwable

import scribe.message.LoggableMessage
import scribe.output.{CompositeOutput, EmptyOutput, LogOutput, TextOutput}

import scala.annotation.tailrec

case class Trace(className: String,
                 message: Option[String],
                 elements: List[TraceElement],
                 cause: Option[Trace])

case class TraceLoggableMessage(throwable: Throwable) extends LoggableMessage {
  override lazy val value: Trace = Trace.throwable2Trace(throwable)

  override lazy val logOutput: LogOutput = Trace.toLogOutput(EmptyOutput, value)
}

object Trace {
  private val NativeMethod: Int = -2

  def throwable2Trace(throwable: Throwable): Trace = {
    val elements = throwable.getStackTrace.toList.map { e =>
      TraceElement(e.getClassName, e.getFileName, e.getMethodName, e.getLineNumber)
    }
    val message = throwable.getLocalizedMessage match {
      case null | "" => None
      case m => Some(m)
    }
    Trace(throwable.getClass.getName, message, elements, Option(throwable.getCause).map(throwable2Trace))
  }

  @tailrec
  final def toLogOutput(message: LogOutput,
                        trace: Trace,
                        primaryCause: Boolean = true,
                        b: StringBuilder = new StringBuilder): LogOutput = {
    if (!primaryCause) {
      b.append("Caused by: ")
    }
    b.append(trace.className)
    trace.message.foreach { message =>
      b.append(": ")
      b.append(message)
    }
    b.append(scribe.lineSeparator)
    writeStackTrace(b, trace.elements)
    trace.cause match {
      case Some(cause) => toLogOutput(message, cause, primaryCause = false, b = b)
      case None =>
        val output = new TextOutput(b.toString())
        if (message == EmptyOutput) {
          output
        } else {
          new CompositeOutput(List(message, new TextOutput(scribe.lineSeparator), output))
        }
    }
  }

  @tailrec
  private def writeStackTrace(b: StringBuilder, elements: List[TraceElement]): Unit = {
    elements.headOption match {
      case None => // No more elements
      case Some(head) =>
        b.append("\tat ")
        b.append(head.`class`)
        b.append('.')
        b.append(head.method)
        b.append('(')
        if (head.line == NativeMethod) {
          b.append("Native Method")
        } else {
          b.append(head.fileName)
          if (head.line > 0) {
            b.append(':')
            b.append(head.line)
          }
        }
        b.append(')')
        b.append(scribe.lineSeparator)
        writeStackTrace(b, elements.tail)
    }
  }
}