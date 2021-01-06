package scribe.file

import scribe.file.writer.LogFileWriter

import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.atomic.AtomicLong

import scala.jdk.CollectionConverters._

case class LogFile private(path: Path,
                           append: Boolean,
                           flushMode: FlushMode,
                           charset: Charset) {
  private lazy val sizeCounter = new AtomicLong(0L)
  @volatile private var status: LogFileStatus = LogFileStatus.Inactive
  private lazy val writer: LogFileWriter = {
    status = LogFileStatus.Active
    Option(path.getParent).foreach(Files.createDirectories(_))
    if (Files.exists(path)) {
      sizeCounter.set(Files.size(path))
    }
    LogFileWriter(this)
  }

  final def write(output: String): Unit = if (output == None.orNull) {
    writer.write("null")
    flushMode.dataWritten(this, writer)
    sizeCounter.addAndGet(4)
  } else {
    writer.write(output)
    flushMode.dataWritten(this, writer)
    sizeCounter.addAndGet(output.length.toLong)
  }

  def flush(): Unit = if (status == LogFileStatus.Active) {
    writer.flush()
  }

  def dispose(): Unit = if (status == LogFileStatus.Active) {
    status = LogFileStatus.Disposed
    writer.dispose()
  }
}

trait PathPart {
  def current(previous: Path): Path
  def all(previous: Path): List[Path]
}

object PathPart {
  case object Root extends PathPart {
    override def current(previous: Path): Path = Paths.get("/")

    override def all(previous: Path): List[Path] = List(current(previous))
  }

  case class Static(part: String) extends PathPart {
    override def current(previous: Path): Path = previous.resolve(part)

    override def all(previous: Path): List[Path] = List(current(previous))
  }

  case class Matcher(matcher: Path => Boolean, apply: Path => Path) extends PathPart {
    override def current(previous: Path): Path = apply(previous)

    override def all(previous: Path): List[Path] = Files.list(previous).iterator().asScala.toList.filter(matcher)
  }
}

sealed trait LogFileStatus

object LogFileStatus {
  case object Inactive extends LogFileStatus
  case object Active extends LogFileStatus
  case object Disposed extends LogFileStatus
}

object LogFile {
  private var paths = Map.empty[Path, LogFile]
  private var usage = Map.empty[LogFile, Set[FileWriter]]

  def request(path: Path, writer: FileWriter): LogFile = synchronized {
    val logFile = paths.get(path) match {
      case Some(lf) => lf
      case None =>
        val absolutePath = path.toAbsolutePath
        paths.get(absolutePath) match {
          case Some(lf) =>
            paths += path -> lf
            lf
          case None =>
            val lf = new LogFile(absolutePath, writer.append, writer.flushMode, writer.charset)
            paths += path -> lf
            paths += absolutePath -> lf
            lf
        }
    }
    val set = usage.getOrElse(logFile, Set.empty) + writer
    usage += logFile -> set
    logFile
  }

  def release(logFile: LogFile, writer: FileWriter): Unit = synchronized {
    val set = usage.getOrElse(logFile, Set.empty) - writer
    if (set.isEmpty) {
      logFile.flush()
      logFile.dispose()
      usage -= logFile
    } else {
      usage += logFile -> set
    }
  }
}