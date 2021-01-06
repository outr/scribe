package scribe.file

import scribe.{LogRecord, Priority}
import scribe.output.LogOutput
import scribe.output.format.OutputFormat

import java.nio.charset.Charset
import java.nio.file.Path

case class FileWriter(append: Boolean = true,
                      flushMode: FlushMode = FlushMode.AsynchronousFlush(),
                      charset: Charset = Charset.defaultCharset(),
                      actions: List[FileHandler]) {

}

trait FileHandler {
  def beforeWrite[M](logFile: LogFile,
                     writer: FileWriter,
                     record: LogRecord[M],
                     output: LogOutput,
                     outputFormat: OutputFormat): List[FileAction]

  def afterWrite[M](logFile: LogFile,
                    writer: FileWriter,
                    record: LogRecord[M],
                    output: LogOutput,
                    outputFormat: OutputFormat): List[FileAction]

  def priority: Priority = Priority.Normal
}

sealed trait HandlerResult

object HandlerResult {
  case object Continue extends HandlerResult
  case object Stop extends HandlerResult
  case object Restart extends HandlerResult
}

sealed trait FileAction

object FileAction {
  case object BackupFile extends FileAction
  case class ChangePath(path: Path) extends FileAction
  case object DeleteFile extends FileAction
  case class MoveFile(path: Path) extends FileAction
}