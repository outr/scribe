package scribe.logstash

case class LogstashRecord(message: String,
                          service: String,
                          level: String,
                          value: Double,
                          throwable: Option[String],
                          fileName: String,
                          className: String,
                          methodName: Option[String],
                          line: Option[Int],
                          thread: String,
                          `@timestamp`: String,
                          mdc: Map[String, String])