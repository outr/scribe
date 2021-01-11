package scribe.file

import scribe.file.handler.{FileHandler, WriteStatus}
import scribe.{LogRecord, Priority}
import scribe.output.LogOutput
import scribe.output.format.OutputFormat
import scribe.util.Time
import scribe.writer.Writer

import java.nio.charset.Charset
import java.nio.file.{Files, Path}

case class FileWriter(pathBuilder: PathBuilder = PathBuilder.Default,
                      append: Boolean = true,
                      flushMode: FlushMode = FlushMode.AsynchronousFlush(),
                      charset: Charset = Charset.defaultCharset()) extends Writer {
  private var _path: Path = pathBuilder.path(Time())

  def path: Path = _path

  def list(): List[Path] = pathBuilder.list().sortBy(path => Files.getLastModifiedTime(path))

  def updatePath(): Boolean = {
    val newPath = pathBuilder.path(Time())
    _path = newPath
    _path != newPath
  }

  override def write[M](record: LogRecord[M], output: LogOutput, outputFormat: OutputFormat): Unit = synchronized {
    pathBuilder.before(this)

    // Write to LogFile
    val logFile = LogFile(this)
    outputFormat.begin(logFile.write)
    outputFormat(output, logFile.write)
    outputFormat.end(logFile.write)
    logFile.write(System.lineSeparator())

    pathBuilder.after(this)
  }

  def flushNever: FileWriter = copy(flushMode = FlushMode.NeverFlush)
  def flushAlways: FileWriter = copy(flushMode = FlushMode.AlwaysFlush)
  def flushAsync: FileWriter = copy(flushMode = FlushMode.AsynchronousFlush())

//  def staticPath(path: Path): FileWriter = withPathBuilder(PathBuilder(List(PathPart.SetPath(path))))
}