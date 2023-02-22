package scribe.json

import io.circe.syntax.EncoderOps
import io.circe.{Json, JsonObject}
import scribe.{LogRecord, lineSeparator}
import scribe.data.MDC
import scribe.message.Message

import java.time.{Instant, OffsetDateTime, ZoneId}
object ScribeCirceJsonSupport extends ScribeJsonSupport[Json] {
  def json2String(json: Json): String = json.noSpaces

  def logRecord2Json(record: LogRecord): Json = {
    val traces = record.messages
      .collect { case message: Message[_] if message.value.isInstanceOf[Throwable] => message }
      .map(_.logOutput.plainText)
    val messages = record.messages
      .collect { case message: Message[_] if !message.value.isInstanceOf[Throwable] => message }
      .map(_.logOutput.plainText)
    val timestamp = OffsetDateTime.ofInstant(Instant.ofEpochMilli(record.timeStamp), ZoneId.of("UTC"))
    val service = MDC.get("service").map(_.toString)
    JsonObject(
      "messages" -> messages.asJson,
      "service" -> service.asJson,
      "level" -> record.level.name.asJson,
      "value" -> record.levelValue.asJson,
      "fileName" -> record.fileName.asJson,
      "className" -> record.className.asJson,
      "methodName" -> record.methodName.asJson,
      "line" -> record.line.asJson,
      "thread" -> record.thread.getName.asJson,
      "@timestamp" -> timestamp.asJson,
      "stack_trace" -> (if (traces.isEmpty) Json.Null else traces.mkString(lineSeparator).asJson),
      "mdc" -> JsonObject.fromMap(MDC.map.map { case (key, function) =>
        key -> function().toString.asJson
      }).asJson,
      "data" -> JsonObject.fromMap(record.data.map { case (key, function) =>
        key -> function().toString.asJson
      }).asJson
    ).asJson
  }
}
