package scribe.file

import scribe.{LogRecord, Priority}
import scribe.output.LogOutput
import scribe.output.format.OutputFormat
import scribe.writer.Writer

import java.nio.charset.Charset
import java.nio.file.Path

case class FileWriter(append: Boolean = true,
                      flushMode: FlushMode = FlushMode.AsynchronousFlush(),
                      charset: Charset = Charset.defaultCharset(),
                      handlers: List[FileHandler]) extends Writer {
  private lazy val state = FileWriterState[Any](this, None.orNull, None.orNull, None.orNull, Nil, PathBuilder.Default)

  private var _path: Path = state.pathBuilder.path
  def path: Path = _path

  override def write[M](record: LogRecord[M], output: LogOutput, outputFormat: OutputFormat): Unit = synchronized {
    // Reset the state
    state.set(record, output, outputFormat)
    // Apply all handlers before write
    handlers.foreach(_.beforeWrite(state.asInstanceOf[FileWriterState[M]]))
    // Apply FileActions before
    state.actions.foreach(invoke)
    // Write to LogFile
    state.outputFormat(state.output, state.logFile.write)
    // Reset the actions before afterWrite
    state.actions = Nil
    // Apply all handlers after write
    handlers.foreach(_.afterWrite(state.asInstanceOf[FileWriterState[M]]))
    // Apply FileActions after
    state.actions.foreach(invoke)
  }

  private def invoke(action: FileAction): Unit = action match {
    case FileAction.ChangePath(path) => _path = path
    case FileAction.DeleteFile => LogFile.delete(state.logFile)
    case FileAction.MoveFile(path: Path) => LogFile.move(state.logFile, path)
  }
}

case class FileWriterState[M](writer: FileWriter,
                              var record: LogRecord[M],
                              var output: LogOutput,
                              var outputFormat: OutputFormat,
                              var actions: List[FileAction],
                              var pathBuilder: PathBuilder) {
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
  case class ChangePath(path: Path) extends FileAction
  case object DeleteFile extends FileAction
  case class MoveFile(path: Path) extends FileAction
}