package scribe.file

import scribe.LogRecord
import scribe.output.LogOutput
import scribe.output.format.OutputFormat
import scribe.util.Time
import scribe.writer.Writer

import java.io.File
import java.nio.charset.Charset
import scala.concurrent.ExecutionContext

case class FileWriter(pathBuilder: PathBuilder = PathBuilder.Default,
                      append: Boolean = true,
                      flushMode: FlushMode = FlushMode.AsynchronousFlush()(scribe.Execution.global),
                      charset: Charset = Charset.defaultCharset()) extends Writer {
  private var previousFile: Option[File] = None
  private var _file: File = resolveFile()

  def file: File = _file

  def list(): List[File] = pathBuilder.iterator().toList.sortBy(_.lastModified())

  def resolveFile(): File = pathBuilder.file(Time())

  def updatePath(): Unit = {
    val newFile = resolveFile()
    _file = newFile
  }

  override def write[M](record: LogRecord[M], output: LogOutput, outputFormat: OutputFormat): Unit = synchronized {
    pathBuilder.before(this)

    // Write to LogFile
    val logFile = LogFile(this)
    if (!previousFile.contains(_file)) {
      previousFile = Some(_file)
      if (_file.length() == 0L || !append) {
        outputFormat.init(logFile.write)
      }
    }
    outputFormat.begin(logFile.write)
    outputFormat(output, logFile.write)
    outputFormat.end(logFile.write)
    logFile.write(System.lineSeparator())

    pathBuilder.after(this)
  }

  def flush(): Unit = LogFile(this).flush()

  def flushNever: FileWriter = copy(flushMode = FlushMode.NeverFlush)
  def flushAlways: FileWriter = copy(flushMode = FlushMode.AlwaysFlush)
  def flushAsync(implicit ec: ExecutionContext): FileWriter = copy(flushMode = FlushMode.AsynchronousFlush())
}