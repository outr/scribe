package scribe.json

import fabric._
import fabric.rw._
import fabric.io.JsonFormatter
import scribe.LogRecord
import scribe.data.MDC
import scribe.message.Message
import scribe.throwable.{Trace, TraceElement}
import perfolation._

object ScribeFabricJsonSupport extends ScribeJsonSupport[Json] {
  private implicit val traceElementRW: RW[TraceElement] = RW.gen
  private implicit val traceRW: RW[Trace] = RW.gen

  override def json2String(json: Json): String = JsonFormatter.Compact(json)

  override def logRecord2Json(record: LogRecord): Json = {
    val l = record.timeStamp
    val traces = record.messages.map(_.value).collect {
      case trace: Trace => trace
    } match {
      case Nil => Null
      case t :: Nil => t.json
      case list => list.json
    }
    val messages = record.messages.collect {
      case message: Message[_] if !message.value.isInstanceOf[Throwable] => message.value match {
        case json: Json => json
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
      "methodName" -> record.methodName.map(_.json).getOrElse(Null),
      "line" -> record.line.map(_.json).getOrElse(Null),
      "column" -> record.column.map(_.json).getOrElse(Null),
      "data" -> data.map {
        case (key, value) => value() match {
          case json: Json => key -> json
          case any => key -> str(any.toString)
        }
      },
      "mdc" -> MDC.map.map {
        case (key, value) => value() match {
          case json: Json => key -> json
          case any => key -> str(any.toString)
        }
      },
      "trace" -> traces,
      "timeStamp" -> l,
      "date" -> l.t.F,
      "time" -> s"${l.t.T}.${l.t.L}${l.t.z}"
    )
  }
}
