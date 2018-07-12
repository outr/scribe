package scribe.logstash

import io.youi.client.HttpClient
import io.youi.http.{Content, HttpRequest, HttpResponse, Method}
import io.youi.net._
import profig.JsonUtil
import scribe.{LogRecord, MDC}
import scribe.writer.Writer
import perfolation._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

case class LogstashWriter(url: URL,
                          service: String,
                          asynchronous: Boolean = true) extends Writer {
  private lazy val client = HttpClient()

  override def write[M](record: LogRecord[M], output: String): Unit = {
    val future = log(record)
    if (!asynchronous) {
      Await.result(future, 10.seconds)
    }
  }

  def log[M](record: LogRecord[M]): Future[HttpResponse] = {
    val l = record.timeStamp
    val timestamp = s"${l.t.F}T${l.t.T}.${l.t.L}${l.t.z}"
    val r = LogstashRecord(
      message = record.message,
      service = service,
      level = record.level.name,
      value = record.value,
      throwable = record.throwable.map(LogRecord.throwable2String(None, _)),
      fileName = record.fileName,
      className = record.className,
      methodName = record.methodName,
      lineNumber = record.lineNumber,
      thread = record.thread.getName,
      `@timestamp` = timestamp,
      mdc = MDC.map
    )
    val json = JsonUtil.toJsonString(r)
    val content = Content.string(json, ContentType.`application/json`)
    val request = HttpRequest(
      method = Method.Post,
      url = url,
      content = Some(content)
    )
    client.send(request)
  }
}