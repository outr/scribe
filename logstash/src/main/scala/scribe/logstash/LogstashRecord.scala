package scribe.logstash

import fabric.rw._

case class LogstashRecord(message: String,
                          service: String,
                          level: String,
                          value: Double,
                          additionalMessages: List[String],
                          fileName: String,
                          className: String,
                          methodName: Option[String],
                          line: Option[Int],
                          thread: String,
                          `@timestamp`: String,
                          mdc: Map[String, String],
                          data: Map[String, String])

object LogstashRecord {
  implicit val rw: ReaderWriter[LogstashRecord] = ccRW
}