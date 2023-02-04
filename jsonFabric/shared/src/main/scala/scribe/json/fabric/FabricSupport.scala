package scribe.json.fabric

import fabric.Value
import fabric.parse.{Json, JsonWriter}
import fabric.rw.Reader
import scribe.LogRecord
import scribe.json.{JsonEventEncoder, LoggableEvent}
import scribe.message.LoggableMessage
import scribe.output.TextOutput

import scala.language.implicitConversions

trait FabricSupport {
  implicit def fabricJsonEventEncoder[E](implicit reader: Reader[E], le: LoggableEvent[E]): JsonEventEncoder[E] = new JsonEventEncoder[E] {
    implicit val loggableEvent: LoggableEvent[E] = le

    def encode(record: LogRecord, additional: Map[String, String], compact: Boolean): String = {
      val json = reader.read(le.toEvent(record, additional))
      if (compact) JsonWriter.Compact(json) else JsonWriter.Default(json)
    }
  }

  implicit def loggableJson(json: Value): LoggableMessage =
    LoggableMessage[Value](v => new TextOutput(Json.format(v)))(json)
}
