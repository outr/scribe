package scribe.json

import scribe.LogRecord
import scribe.message.LoggableMessage
import scribe.output.format.OutputFormat
import scribe.output.{LogOutput, TextOutput}
import scribe.writer.Writer

import scala.language.implicitConversions

trait ScribeJsonSupport[J] {
  implicit def json2LoggableMessage(json: J): LoggableMessage =
    LoggableMessage[J](json => new TextOutput(json2String(json)))(json)

  def json2String(json: J): String

  def logRecord2Json(record: LogRecord): J

  def writer(writer: Writer): Writer = new Writer {
    override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = {
      val json = logRecord2Json(record)
      val jsonString = json2String(json)
      writer.write(record, new TextOutput(jsonString), outputFormat)
    }
  }
}