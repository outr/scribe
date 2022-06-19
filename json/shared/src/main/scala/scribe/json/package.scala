package scribe

import fabric.Value
import fabric.parse.Json
import scribe.message.LoggableMessage
import scribe.output.TextOutput

import scala.language.implicitConversions

package object json {
  implicit def loggableJson(json: Value): LoggableMessage =
    LoggableMessage[Value](v => new TextOutput(Json.format(v)))(json)
}