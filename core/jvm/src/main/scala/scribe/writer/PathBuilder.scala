package scribe.writer

import java.nio.file.{Files, Path}

import scribe.LogRecord
import scribe.writer.file.LogFile

trait PathBuilder {
  def derivePath[M](writer: FileWriter, record: LogRecord[M]): Option[LogFile]

  protected def createLogFile(writer: FileWriter, path: Path): LogFile = {
    LogFile(path, writer.append, writer.autoFlush, writer.charset, writer.mode)
  }

  protected def validate(writer: FileWriter, newPath: Path): Option[LogFile] = {
    if (FileWriter.isSamePath(writer.logFile.map(_.path), newPath)) {
      None
    } else {
      Some(createLogFile(writer, newPath))
    }
  }
}