import scribe._
import scribe.data.MDC
import scribe.message.LazyMessage

object LogExample extends App {
  MDC.update("key1", "value1")

  val logger = Logger("test")
  logger.log(LogRecord(
    level = Level.Info,
    value = Level.Info.value,
    messages = List("Some message"),
    fileName = "file.scala",
    className = "Class",
    methodName = None,
    line = None,
    column = None,
    data = Map(
      "key2" -> (() => "value2")
    )
  ))
}