package scribe.json

import fabric._
import fabric.rw._
import perfolation._
import scribe.LogRecord
import scribe.data.MDC
import scribe.message.Message
import scribe.output.format.OutputFormat
import scribe.output.{LogOutput, TextOutput}
import scribe.writer.Writer

class JsonWriter(writer: Writer, compact: Boolean = true) extends Writer {
  override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = {
    val r = toJson(record)
    val json = r.toValue
    val jsonString = if (compact) {
      fabric.parse.JsonWriter.Compact(json)
    } else {
      fabric.parse.JsonWriter.Default(json)
    }
    writer.write(record, new TextOutput(jsonString), outputFormat)
  }

  def toJson(record: LogRecord): Value = {
    val l = record.timeStamp
    val traces = record.messages.collect {
      case message: Message[_] if message.value.isInstanceOf[Throwable] => throwable2Trace(message.value.asInstanceOf[Throwable])
    } match {
      case Nil => Null
      case t :: Nil => t.toValue
      case list => list.toValue
    }
    val messages = record.messages.collect {
      case message: Message[_] if !message.value.isInstanceOf[Throwable] => message.value match {
        case v: Value => v
        case _ => Str(message.logOutput.plainText)
      }
    } match {
      case Nil => Null
      case m :: Nil => m
      case list => Arr(list.toVector)
    }
    val data = MDC.map ++ record.data
    obj(
      "level" -> record.level.name,
      "levelValue" -> record.levelValue,
      "message" -> messages,
      "fileName" -> record.fileName,
      "className" -> record.className,
      "methodName" -> record.methodName.map(_.toValue).getOrElse(Null),
      "line" -> record.line.map(_.toValue).getOrElse(Null),
      "column" -> record.column.map(_.toValue).getOrElse(Null),
      "data" -> data.map {
        case (key, value) => value() match {
          case value: Value => key -> value
          case any => key -> str(any.toString)
        }
      },
      "trace" -> traces,
      "timeStamp" -> l,
      "date" -> l.t.F,
      "time" -> s"${l.t.T}.${l.t.L}${l.t.z}"
    )
  }

  private def throwable2Trace(throwable: Throwable): Trace = {
    val elements = throwable.getStackTrace.toList.map { e =>
      TraceElement(e.getClassName, e.getMethodName, e.getLineNumber)
    }
    Trace(throwable.getLocalizedMessage, elements, Option(throwable.getCause).map(throwable2Trace))
  }
}