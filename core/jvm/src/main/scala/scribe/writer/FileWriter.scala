package scribe.writer

import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths}

import scribe._
import scribe.writer.file.{LogFile, LogFileManager, LogFileMode}
import perfolation._

import scala.collection.JavaConverters._

case class FileWriter(pathBuilder: PathBuilder,
                      manager: LogFileManager = LogFileManager.Dispose,
                      mode: LogFileMode = LogFileMode.IO,
                      append: Boolean = true,
                      autoFlush: Boolean = false,
                      charset: Charset = Charset.defaultCharset()) extends Writer {
  @volatile private[writer] var logFile: Option[LogFile] = None

  override def write[M](record: LogRecord[M], output: String): Unit = synchronized {
    pathBuilder.derivePath(this, record).foreach { newLogFile =>
      manager.replace(logFile, newLogFile)
      logFile = Some(newLogFile)
    }
    logFile.foreach(_.write(output))
  }

  def flush(): Unit = logFile.foreach(_.flush())

  override def dispose(): Unit = {
    super.dispose()

    logFile.foreach(_.dispose())
  }
}

object FileWriter {
  object format {
    lazy val daily: Long => String = (l: Long) => {
      p"${l.t.Y}-${l.t.m}-${l.t.d}"
    }
  }

  def simple(fileName: String = "app.log",
             directory: Path = Paths.get("logs"),
             mode: LogFileMode = LogFileMode.IO,
             append: Boolean = true,
             autoFlush: Boolean = false,
             charset: Charset = Charset.defaultCharset()): FileWriter = {
    FileWriter(
      new FlatPathBuilder(directory.resolve(fileName)),
      mode = mode,
      append = append,
      autoFlush = autoFlush,
      charset = charset
    )
  }

  def flat(prefix: String = "app",
           suffix: String = ".log",
           directory: Path = Paths.get("logs"),
           maxLogs: Option[Int] = None,
           maxSize: Option[Long] = None,
           gzip: Boolean = false,
           mode: LogFileMode = LogFileMode.IO,
           append: Boolean = true,
           autoFlush: Boolean = false,
           charset: Charset = Charset.defaultCharset()): FileWriter = {
    val pathLister = (_: Path) => {
      Files.list(directory).iterator().asScala.filter { p =>
        val name = p.getFileName.toString
        val ending = if (gzip) s"$suffix.gz" else suffix
        name.startsWith(prefix) && name.endsWith(ending)
      }.toList.sortBy(Files.getLastModifiedTime(_)).reverse
    }
    val flatPathBuilder = new FlatPathBuilder(directory.resolve(p"$prefix$suffix"))
    val pathBuilder = maxSize match {
      case Some(size) => new MaxSizePathBuilder(size, (p: Path) => {
        val name = p.getFileName.toString
        val pre = name.substring(0, name.length - suffix.length)
        val generator = (i: Int) => p"$pre.$i$suffix"
        MaxSizePathBuilder.findNext(directory, generator)
      }, flatPathBuilder)
      case None => flatPathBuilder
    }
    FileWriter(
      pathBuilder = pathBuilder,
      manager = LogFileManager.Grouped(List(
        Some(LogFileManager.Dispose),
        if (gzip) Some(LogFileManager.GZip()) else None,
        maxLogs.map(max => LogFileManager.MaximumLogs(max, pathLister))
      ).flatten),
      mode = mode,
      append = append,
      autoFlush = autoFlush,
      charset = charset
    )
  }

  def date(prefix: String = "app",
           suffix: String = ".log",
           directory: Path = Paths.get("logs"),
           maxLogs: Option[Int] = None,
           maxSize: Option[Long] = None,
           gzip: Boolean = false,
           formatter: Long => String = format.daily,
           mode: LogFileMode = LogFileMode.IO,
           append: Boolean = true,
           autoFlush: Boolean = false,
           charset: Charset = Charset.defaultCharset()): FileWriter = {
    val pathLister = (_: Path) => {
      Files.list(directory).iterator().asScala.filter { p =>
        val name = p.getFileName.toString
        val ending = if (gzip) s"$suffix.gz" else suffix
        name.startsWith(prefix) && name.endsWith(ending)
      }.toList.sortBy(Files.getLastModifiedTime(_)).reverse
    }
    val dateFormattedPathBuilder = new DateFormattedPathBuilder(directory, (l: Long) => p"$prefix${formatter(l)}$suffix")
    val pathBuilder = maxSize match {
      case Some(size) => new MaxSizePathBuilder(size, (p: Path) => {
        val name = p.getFileName.toString
        val pre = name.substring(0, name.length - suffix.length)
        val generator = (i: Int) => p"$pre.$i$suffix"
        MaxSizePathBuilder.findNext(directory, generator)
      }, dateFormattedPathBuilder)
      case None => dateFormattedPathBuilder
    }
    FileWriter(
      pathBuilder = pathBuilder,
      manager = LogFileManager.Grouped(List(
        Some(LogFileManager.Dispose),
        if (gzip) Some(LogFileManager.GZip()) else None,
        maxLogs.map(max => LogFileManager.MaximumLogs(max, pathLister))
      ).flatten),
      mode = mode,
      append = append,
      autoFlush = autoFlush,
      charset = charset
    )
  }

  def isSamePath(oldPath: Option[Path], newPath: Path): Boolean = oldPath match {
    case Some(current) => if (current == newPath) {
      true
    } else if (Files.exists(current)) {
      if (Files.exists(newPath)) {
        Files.isSameFile(current, newPath)
      } else {
        false
      }
    } else {
      current.toAbsolutePath.toString == newPath.toAbsolutePath.toString
    }
    case None => false
  }
}