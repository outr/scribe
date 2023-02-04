package scribe

import scribe.output.LogOutput
import scribe.output.format.OutputFormat
import scribe.writer.Writer

import scala.language.implicitConversions

package object json {
  trait LoggableEvent[E] {
    def toEvent(record: LogRecord, additional: Map[String, String]): E
  }

  trait JsonEventEncoder[E] {
    def encode(record: LogRecord, additional: Map[String, String], compact: Boolean): String
  }

  class JsonWriter[E](writer: Writer, additional: Map[String, String], compact: Boolean = true)
                     (implicit val encoder: JsonEventEncoder[E]) extends Writer {
    def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit =
      writer.write(record, encoder.encode(record, additional, compact), outputFormat)
  }
}