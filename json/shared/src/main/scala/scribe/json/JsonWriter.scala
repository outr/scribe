package scribe.json

import fabric.parse.Json
import scribe.LogRecord
import scribe.output.{LogOutput, TextOutput}
import scribe.output.format.OutputFormat
import scribe.writer.Writer
import perfolation._
import fabric.rw._

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
    val json = r.toValue
    val jsonString = Json.format(json)
    writer.write(record, new TextOutput(jsonString), outputFormat)
  }

  private def throwable2Trace(throwable: Throwable): Trace = {
    val elements = throwable.getStackTrace.toList.map { e =>
      TraceElement(e.getClassName, e.getMethodName, e.getLineNumber)
    }
    Trace(throwable.getLocalizedMessage, elements, Option(throwable.getCause).map(throwable2Trace))
  }
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

object Record {
  implicit val mapRW: ReaderWriter[Map[String, String]] = ReaderWriter.stringMapRW

  implicit val rw: ReaderWriter[Record] = ccRW
}

case class Trace(message: String, elements: List[TraceElement], cause: Option[Trace])

object Trace {
  implicit val rw: ReaderWriter[Trace] = ccRW
}

case class TraceElement(`class`: String, method: String, line: Int)

object TraceElement {
  implicit val rw: ReaderWriter[TraceElement] = ccRW
}