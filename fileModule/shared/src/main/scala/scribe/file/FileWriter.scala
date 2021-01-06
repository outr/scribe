package scribe.file

import scribe.{LogRecord, Priority}
import scribe.output.{LogOutput, out}
import scribe.output.format.OutputFormat
import scribe.writer.Writer

import java.nio.charset.Charset
import java.nio.file.Path

case class FileWriter(append: Boolean = true,
                      flushMode: FlushMode = FlushMode.AsynchronousFlush(),
                      charset: Charset = Charset.defaultCharset(),
                      handlers: List[FileHandler]) extends Writer {
  private lazy val state = FileWriterState[Any](this, None.orNull, None.orNull, None.orNull, Nil)

  private var _path: Path =
  def path: Path = ???

  override def write[M](record: LogRecord[M], output: LogOutput, outputFormat: OutputFormat): Unit = synchronized {
    state.set(record, output, outputFormat)
    handlers.foreach(_.beforeWrite(state.asInstanceOf[FileWriterState[M]]))
    // TODO: apply actions
    state.actions.foreach(_.)
    // TODO: write
    state.actions = Nil
    handlers.foreach(_.afterWrite(state.asInstanceOf[FileWriterState[M]]))
    // TODO: apply actions
  }
}

case class FileWriterState[M](writer: FileWriter,
                              var record: LogRecord[M],
                              var output: LogOutput,
                              var outputFormat: OutputFormat,
                              var actions: List[FileAction]) {
  def set[T](record: LogRecord[T], output: LogOutput, outputFormat: OutputFormat): Unit = {
    this.record = record.asInstanceOf[LogRecord[M]]
    this.output = output
    this.outputFormat = outputFormat
    actions = Nil
  }

  def logFile: LogFile = LogFile(writer)
}

trait FileHandler {
  def beforeWrite[M](state: FileWriterState[M]): Unit

  def afterWrite[M](state: FileWriterState[M]): Unit

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