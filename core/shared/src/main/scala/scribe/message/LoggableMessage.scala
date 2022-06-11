package scribe.message

import scribe.output.LogOutput

import scala.language.implicitConversions

trait LoggableMessage {
  def value: Any
  def logOutput: LogOutput
}

object LoggableMessage {
  implicit def string2Message(s: String): LoggableMessage = Message.static(s)
  implicit def stringList2Messages(list: List[String]): List[LoggableMessage] = list.map(string2Message)
  implicit def throwable2Message(throwable: Throwable): LoggableMessage = Message.static(throwable)
  implicit def throwableList2Messages(list: List[Throwable]): List[LoggableMessage] = list.map(throwable2Message)
}