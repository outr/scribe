package scribe.writer

import java.nio.file.Path

import scribe.LogRecord
import scribe.writer.file.LogFile

class FlatPathBuilder(path: Path) extends PathBuilder {
  override def derivePath[M](writer: FileWriter, record: LogRecord[M]): Option[LogFile] = validate(writer, path)
}