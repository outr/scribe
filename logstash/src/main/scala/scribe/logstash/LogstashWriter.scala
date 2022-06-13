package scribe.logstash

import fabric.parse.Json
import fabric.rw._
import io.youi.client.HttpClient
import io.youi.http.HttpResponse
import io.youi.http.content.Content
import io.youi.net._
import perfolation._
import scribe.Execution.global
import scribe.LogRecord
import scribe.data.MDC
import scribe.output.LogOutput
import scribe.output.format.OutputFormat
import scribe.writer.Writer

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

case class LogstashWriter(url: URL,
                          service: String,
                          additionalFields: Map[String, String] = Map.empty,
                          asynchronous: Boolean = true) extends Writer {
  private lazy val client = HttpClient.url(url).post

  override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = {
    val future = log(record)
    if (!asynchronous) {
      Await.result(future, 10.seconds)
    }
  }

  def log(record: LogRecord): Future[HttpResponse] = {
    val l = record.timeStamp
    val timestamp = s"${l.t.F}T${l.t.T}.${l.t.L}${l.t.z}"
    val r: LogstashRecord = LogstashRecord(
      messages = record.messages.map(_.logOutput.plainText),
      service = service,
      level = record.level.name,
      value = record.levelValue,
      fileName = record.fileName,
      className = record.className,
      methodName = record.methodName,
      line = record.line,
      thread = record.thread.getName,
      `@timestamp` = timestamp,
      mdc = MDC.map.map {
        case (key, function) => key -> function().toString
      },
      data = record.data.map {
        case (key, function) => key -> function().toString
      }
    )

    val value = r.toValue
    val additional = additionalFields.toValue
    val json = Json.format(value.merge(additional))

    val content = Content.string(json, ContentType.`application/json`)
    client.content(content).send()
  }
}