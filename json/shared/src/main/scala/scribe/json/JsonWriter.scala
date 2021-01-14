package scribe.json

import scribe.LogRecord
import scribe.output.{LogOutput, TextOutput}
import scribe.output.format.OutputFormat
import scribe.writer.Writer
import perfolation._
import upickle.default._
import JsonWriter._

case class JsonWriter(writer: Writer) extends Writer {
  override def write[M](record: LogRecord[M], output: LogOutput, outputFormat: OutputFormat): Unit = {
    val l = record.timeStamp
    val trace = record.throwable.map(throwable2Trace)
    val r = Record(
      level = record.level.name,
      levelValue = record.levelValue,
      message = record.logOutput.plainText,
      fileName = record.fileName,
      className = record.className,
      methodName = record.methodName,
      line = record.line,
      column = record.column,
      data = record.data.map {
        case (key, value) => key -> value().toString
      },
      throwable = trace,
      timeStamp = l,
      date = l.t.F,
      time = s"${l.t.T}.${l.t.L}${l.t.z}"
    )
    val json = writeJs(r)
    val jsonString = json.render()
    writer.write(record, new TextOutput(jsonString), outputFormat)
  }

  private def throwable2Trace(throwable: Throwable): Trace = {
    val elements = throwable.getStackTrace.toList.map { e =>
      TraceElement(e.getClassName, e.getMethodName, e.getLineNumber)
    }
    Trace(throwable.getLocalizedMessage, elements, Option(throwable.getCause).map(throwable2Trace))
  }
}

object JsonWriter {
  implicit def traceElementRW: ReadWriter[TraceElement] = macroRW
  implicit def traceRW: ReadWriter[Trace] = macroRW
  implicit def recordRW: ReadWriter[Record] = macroRW
}

case class Record(level: String,
                  levelValue: Double,
                  message: String,
                  fileName: String,
                  className: String,
                  methodName: Option[String],
                  line: Option[Int],
                  column: Option[Int],
                  data: Map[String, String],
                  throwable: Option[Trace],
                  timeStamp: Long,
                  date: String,
                  time: String)

case class Trace(message: String, elements: List[TraceElement], cause: Option[Trace])

case class TraceElement(`class`: String, method: String, line: Int)