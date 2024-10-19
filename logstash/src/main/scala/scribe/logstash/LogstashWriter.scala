package scribe.logstash

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.instances.future
import fabric.io.JsonFormatter
import fabric.rw._
import perfolation._
import scribe.LogRecord
import scribe.mdc.MDC
import scribe.output.LogOutput
import scribe.output.format.OutputFormat
import scribe.writer.Writer
import spice.http.HttpResponse
import spice.http.client.HttpClient
import spice.http.content.Content
import spice.net.{ContentType, URL}

case class LogstashWriter(url: URL,
                          service: String,
                          additionalFields: Map[String, String] = Map.empty,
                          asynchronous: Boolean = true) extends Writer {
  private lazy val client = HttpClient.url(url).post

  override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = {
    val io = log(record) // Does nothing
    if (!asynchronous) {
      io.unsafeRunSync()
    }
  }

  def log(record: LogRecord): IO[HttpResponse] = {
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

    val json = r.json
    val additional = additionalFields.json

    val content = Content.json(json.merge(additional))
    client.content(content).send()
  }
}
