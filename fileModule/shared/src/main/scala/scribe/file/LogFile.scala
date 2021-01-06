package scribe.file

import scribe.file.writer.LogFileWriter

import java.nio.charset.Charset
import java.nio.file.{Files, Path}
import java.util.concurrent.atomic.AtomicLong

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
      paths.foreach {
        case (key, value) => if (value eq logFile) paths -= key
      }
    } else {
      usage += logFile -> set
    }
  }
}