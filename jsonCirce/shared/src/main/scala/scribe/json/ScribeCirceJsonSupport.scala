package scribe.json

import io.circe.Json.Null
import io.circe.syntax.EncoderOps
import io.circe.{Json, JsonObject}
import perfolation.long2Implicits
import scribe.{LogRecord, lineSeparator}
import scribe.mdc.MDC
import scribe.message.Message
import scribe.throwable.Trace

import io.circe.generic.auto._

object ScribeCirceJsonSupport extends ScribeJsonSupport[Json] {
  def json2String(json: Json): String = json.noSpaces

  override def logRecord2Json(record: LogRecord): Json = {
    val l = record.timeStamp
    val traces = record.messages.map(_.value).collect {
      case trace: Trace => trace
    } match {
      case Nil => Null
      case t :: Nil => t.asJson
      case list => list.asJson
    }
    val messages = record.messages.collect {
      case message: Message[_] if !message.value.isInstanceOf[Throwable] => message.value match {
        case json: Json => json
        case _ => message.logOutput.plainText.asJson
      }
    } match {
      case Nil => Null
      case m :: Nil => m
      case list => list.toVector.asJson
    }
    val data = MDC.map ++ record.data
    JsonObject(
      "level" -> record.level.name.asJson,
      "levelValue" -> record.levelValue.asJson,
      "message" -> messages,
      "fileName" -> record.fileName.asJson,
      "className" -> record.className.asJson,
      "methodName" -> record.methodName.map(_.asJson).getOrElse(Null),
      "line" -> record.line.map(_.asJson).getOrElse(Null),
      "column" -> record.column.map(_.asJson).getOrElse(Null),
      "data" -> data.toList.map {
        case (key, value) => value() match {
          case json: Json => key -> json
          case any => key -> any.toString.asJson
        }
      }.asJson,
      "mdc" -> MDC.map.map {
        case (key, value) => value() match {
          case json: Json => key -> json
          case any => key -> any.toString.asJson
        }
      }.asJson,
      "trace" -> traces,
      "timeStamp" -> l.asJson,
      "date" -> l.t.F.asJson,
      "time" -> s"${l.t.T}.${l.t.L}${l.t.z}".asJson
    )
  }.asJson
}
