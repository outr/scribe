package scribe

import fabric.Json
import fabric.io.JsonFormatter
import scribe.message.LoggableMessage
import scribe.output.TextOutput

import scala.language.implicitConversions

package object json {
  implicit def loggableJson(json: Json): LoggableMessage =
    LoggableMessage[Json](v => new TextOutput(JsonFormatter.Compact(v)))(json)
}