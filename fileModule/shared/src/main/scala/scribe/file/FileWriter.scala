package scribe.file

import scribe.file.handler.{FileHandler, WriteStatus}
import scribe.{LogRecord, Priority}
import scribe.output.LogOutput
import scribe.output.format.OutputFormat
import scribe.util.Time
import scribe.writer.Writer

import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths}
import perfolation._

import scala.jdk.CollectionConverters._

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

  def staticPath(path: Path): FileWriter = copy(pathBuilder = PathBuilder(List(PathPart.SetPath(path))))

  def dailyPath(prefix: => String = "app",
                separator: String = "-",
                extension: String = "log",
                directory: => Path = Paths.get("logs"),
                priority: Priority = Priority.Normal): FileWriter = {
    val handler = FileHandler.before(priority)(FileHandler.daily { writer =>
      writer.updatePath()
    })
    val pathBuilder = PathBuilder(List(new PathPart {
      override def current(previous: Path): Path = {
        val l = Time()
        val distinction = s"${l.t.Y}$separator${l.t.m}$separator${l.t.d}"
        directory.resolve(s"$prefix$separator$distinction.$extension")
      }

      override def all(previous: Path): List[Path] = {
        val regex = s"$prefix[$separator]\\d{4}[$separator]\\d{2}[$separator]\\d{2}[.]$extension"
        Files.list(directory).iterator().asScala.toList.filter { path =>
          val fileName = path.getFileName.toString
          fileName.matches(regex)
        }
      }
    }))
    copy(pathBuilder = pathBuilder).withHandler(handler)
  }
}