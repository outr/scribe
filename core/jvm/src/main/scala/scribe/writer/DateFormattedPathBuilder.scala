package scribe.writer

import java.nio.file.Path

import scribe.LogRecord
import scribe.writer.file.LogFile

class DateFormattedPathBuilder(directory: Path, formatter: Long => String) extends PathBuilder {
  override def derivePath[M](writer: FileWriter, record: LogRecord[M]): Option[LogFile] = {
    val fileName = formatter(record.timeStamp)
    val path = directory.resolve(fileName)
    validate(writer, path)
  }
}