package scribe.json.event

import scribe.LogRecord
import scribe.data.MDC
import scribe.json.LoggableEvent
import perfolation._

case class TraceElement(`class`: String, method: String, line: Int)

case class Trace(message: String, elements: List[TraceElement], cause: Option[Trace])

case class DataDogRecord(
                          level: String,
                          levelValue: Double,
                          // changed
                          message: Either[String, List[String]],
                          fileName: String,
                          className: String,
                          methodName: Option[String],
                          line: Option[Int],
                          column: Option[Int],
                          data: Map[String, String],
                          trace: Either[Trace, List[Trace]],
                          timeStamp: Long,
                          date: String,
                          time: String
                        )

trait DataDogSupport {
  private def throwable2Trace(throwable: Throwable): Trace = {
    val elements = throwable.getStackTrace.toList.map { e =>
      TraceElement(e.getClassName, e.getMethodName, e.getLineNumber)
    }
    Trace(throwable.getLocalizedMessage, elements, Option(throwable.getCause).map(throwable2Trace))
  }

  private def headOrAll[T](xs: List[T]): Either[T, List[T]] = xs match {
    case head :: Nil => Left(head)
    case xs@_ :: _ => Right(xs)
    case Nil => Right(List.empty)
  }

  implicit def dataDogRecordLoggableEvent: LoggableEvent[DataDogRecord] = (record: LogRecord, _: Map[String, String]) => {
    val l = record.timeStamp
    val (traces, messages) = record.messages.partitionMap(message => message.value match {
      case throwable: Throwable =>
        Left(throwable2Trace(throwable))
      case _ =>
        Right(message.logOutput.plainText)
    }
    )
    val data = MDC.map ++ record.data
    DataDogRecord(
      record.level.name,
      record.levelValue,
      headOrAll(messages),
      record.fileName,
      record.className,
      record.methodName,
      record.line,
      record.column,
      data.view.mapValues(_.apply().toString).toMap,
      headOrAll(traces),
      l,
      l.t.F,
      s"${l.t.T}.${l.t.L}${l.t.z}"
    )
  }
}
