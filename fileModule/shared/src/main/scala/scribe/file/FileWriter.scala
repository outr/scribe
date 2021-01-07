package scribe.file

import scribe.file.handler.{FileHandler, WriteStatus}
import scribe.{LogRecord, Priority}
import scribe.output.LogOutput
import scribe.output.format.OutputFormat
import scribe.util.Time
import scribe.writer.Writer

import java.nio.charset.Charset
import java.nio.file.{Files, Path}

case class FileWriter(append: Boolean = true,
                      flushMode: FlushMode = FlushMode.AsynchronousFlush(),
                      charset: Charset = Charset.defaultCharset(),
                      pathBuilder: PathBuilder = PathBuilder.Default,
                      handlers: List[FileHandler] = Nil) extends Writer {
  private var _path: Path = pathBuilder.path

  def path: Path = _path

  def list(): List[Path] = pathBuilder.list().sortBy(path => Files.getLastModifiedTime(path))

  def updatePath(): Boolean = {
    val newPath = pathBuilder.path
    _path = newPath
    _path != newPath
  }

  override def write[M](record: LogRecord[M], output: LogOutput, outputFormat: OutputFormat): Unit = synchronized {
    // Apply all handlers before write
    handlers.foreach(_.apply(WriteStatus.Before, this))
    // Check if the Path should be revalidated
    if (pathBuilder.revalidate()) {
      updatePath()
    }
    // Write to LogFile
    val logFile = LogFile(this)
    outputFormat.begin(logFile.write)
    outputFormat(output, logFile.write)
    outputFormat.end(logFile.write)
    logFile.write(System.lineSeparator())
    // Apply all handlers after write
    handlers.foreach(_.apply(WriteStatus.After, this))
  }

  def withHandler(handler: FileHandler): FileWriter = copy(handlers = (handlers ::: List(handler)).sorted)

  def flushNever: FileWriter = copy(flushMode = FlushMode.NeverFlush)
  def flushAlways: FileWriter = copy(flushMode = FlushMode.AlwaysFlush)
  def flushAsync: FileWriter = copy(flushMode = FlushMode.AsynchronousFlush())

  def withPathBuilder(pathBuilder: PathBuilder): FileWriter = copy(pathBuilder = pathBuilder)

  def staticPath(path: Path): FileWriter = copy(pathBuilder = PathBuilder(List(PathPart.SetPath(path))))

  def withUpdatePathCheck(update: => Boolean): FileWriter = {
    val handler = new FileHandler {
      override def apply(status: WriteStatus, writer: FileWriter): Unit = if (status == WriteStatus.Before) {
        if (update) {
          writer.updatePath()
        }
      }
    }
    withHandler(handler)
  }

  def withUpdatePathChanged[T](get: => T): FileWriter = {
    var previous = Option.empty[T]
    withUpdatePathCheck {
      val current: T = get
      if (!previous.contains(current)) {
        previous = Some(current)
        true
      } else {
        false
      }
    }
  }
}