package scribe.logstash

import io.circe.Json
import io.youi.client.HttpClient
import io.youi.http.content.Content
import io.youi.http.HttpResponse
import io.youi.net._
import profig.JsonUtil
import scribe.{LogRecord, MDC}
import scribe.writer.Writer
import perfolation._
import scribe.output.{EmptyOutput, LogOutput}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scribe.Execution.global

case class LogstashWriter(url: URL,
                          service: String,
                          additionalFields: Map[String, String] = Map.empty,
                          asynchronous: Boolean = true) extends Writer {
  private lazy val client = HttpClient.url(url).post

  override def write[M](record: LogRecord[M], output: LogOutput): Unit = {
    val future = log(record)
    if (!asynchronous) {
      Await.result(future, 10.seconds)
    }
  }

  def log[M](record: LogRecord[M]): Future[HttpResponse] = {
    val l = record.timeStamp
    val timestamp = p"${l.t.F}T${l.t.T}.${l.t.L}${l.t.z}"
    val r = LogstashRecord(
      message = record.message.plainText,
      service = service,
      level = record.level.name,
      value = record.value,
      throwable = record.throwable.map(LogRecord.throwable2LogOutput(EmptyOutput, _).plainText),
      fileName = record.fileName,
      className = record.className,
      methodName = record.methodName,
      line = record.line,
      thread = record.thread.getName,
      `@timestamp` = timestamp,
      mdc = MDC.map.map {
        case (key, function) => key -> function()
      }
    )

    val jsonObj = JsonUtil.toJson(r).asObject.get
    val jsonWithFields = additionalFields.foldLeft(jsonObj) { (obj, field) =>
      obj.add(field._1, Json.fromString(field._2))
    }
    val json = Json.fromJsonObject(jsonWithFields).noSpaces

    val content = Content.string(json, ContentType.`application/json`)
    client.content(content).send()
  }
}
