package scribe.file

import scribe.file.writer.LogFileWriter

import java.io.{File, FileInputStream, FileOutputStream, InputStream, OutputStream}
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.nio.file.{Files, StandardOpenOption}
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.GZIPOutputStream
import scala.annotation.tailrec
import scala.util.Try

case class LogFile private(file: File,
                           append: Boolean,
                           flushMode: FlushMode,
                           charset: Charset) {
  private lazy val sizeCounter = new AtomicLong(0L)
  @volatile private var status: LogFileStatus = LogFileStatus.Inactive
  private lazy val writer: LogFileWriter = {
    status = LogFileStatus.Active
    Option(file.getParentFile).foreach(_.mkdirs())
    if (file.exists()) {
      sizeCounter.set(file.length())
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

  def size: Long = sizeCounter.get()

  def flush(): Unit = if (status == LogFileStatus.Active) {
    writer.flush()
  }

  private def dispose(): Unit = if (status == LogFileStatus.Active) {
    status = LogFileStatus.Disposed
    writer.dispose()
  }
}

object LogFile {
  private var files = Map.empty[File, LogFile]
  private var usage = Map.empty[LogFile, Set[FileWriter]]
  private var current = Map.empty[FileWriter, LogFile]

  /**
    * Make sure that all log files are flushed and closed properly before terminating - Not supported on ScalaNative, so
    * we ignore the error
    */
  Platform.addShutdownHook(dispose())

  // TODO: represent a virtual list of files to avoid having to build paths and update that list with the methods below

  def get(file: File): Option[LogFile] = files.get(file)

  def close(logFile: LogFile): Unit = synchronized {
    logFile.flush()
    files.foreach {
      case (path, lf) => if (lf eq logFile) files -= path
    }
    usage -= logFile
    current.foreach {
      case (fileWriter, lf) => if (lf eq logFile) current -= fileWriter
    }
    logFile.dispose()
  }

  def delete(logFile: LogFile): Unit = synchronized {
    close(logFile)
    delete(logFile.file)
  }

  def delete(file: File): Unit = synchronized {
    if (file.exists()) {
      if (!file.delete()) {
        file.deleteOnExit()
      }
    }
  }

  def move(logFile: LogFile, file: File): Unit = synchronized {
    close(logFile)
    move(logFile.file, file)
  }

  def move(from: File, to: File): Unit = synchronized {
    if (from.exists()) {
      if (to.exists()) {
        to.delete()
      }
      from.renameTo(to)
    }
  }

  def copy(logFile: LogFile, file: File): Unit = synchronized {
    close(logFile)
    copy(logFile.file, file)
  }

  def copy(from: File, to: File): Unit = synchronized {
    if (from.exists()) {
      if (to.exists()) {
        to.delete()
      }
      Files.copy(from.toPath, to.toPath)
    }
  }

  def truncate(logFile: LogFile): Unit = synchronized {
    close(logFile)
    truncate(logFile.file)
  }

  def truncate(file: File): Unit = synchronized {
    val fc = FileChannel.open(file.toPath, StandardOpenOption.WRITE)
    try {
      fc.truncate(0L)
    } finally {
      fc.close()
    }
  }

  def gzip(logFile: LogFile, file: File, deleteOriginal: Boolean, bufferSize: Int): Unit = {
    close(logFile)
    gzip(logFile.file, file, deleteOriginal, bufferSize)
  }

  def gzip(current: File,
           path: File,
           deleteOriginal: Boolean,
           bufferSize: Int): Unit = synchronized {
    if (current.exists()) {
      if (path.exists()) {
        path.delete()
      }
      val buffer = new Array[Byte](bufferSize)
      val input = new FileInputStream(current)
      val output = new GZIPOutputStream(new FileOutputStream(path))
      try {
        stream(input, output, buffer)
        output.flush()
      } finally {
        Try(input.close())
        Try(output.close())
        if (deleteOriginal) {
          if (!current.delete()) {
            current.deleteOnExit()
          }
        }
      }
    }
  }

  @tailrec
  private def stream(input: InputStream, output: OutputStream, buffer: Array[Byte]): Unit = {
    val len = input.read(buffer)
    if (len <= 0) {
      // Finished
    } else {
      output.write(buffer, 0, len)
      stream(input, output, buffer)
    }
  }

  def apply(writer: FileWriter): LogFile = synchronized {
    val file = writer.file
    val logFile = request(file, writer)
    current.get(writer) match {
      case Some(lf) if lf eq logFile => // Nothing to do
      case Some(lf) => {
        release(lf, writer)
        current += writer -> logFile
      }
      case None => current += writer -> logFile
    }
    logFile
  }

  private def request(file: File, writer: FileWriter): LogFile = {
    val logFile = files.get(file) match {
      case Some(lf) => lf
      case None =>
        val absolutePath = file.getAbsoluteFile
        files.get(absolutePath) match {
          case Some(lf) =>
            files += file -> lf
            lf
          case None =>
            val lf = new LogFile(absolutePath, writer.append, writer.flushMode, writer.charset)
            files += file -> lf
            files += absolutePath -> lf
            lf
        }
    }
    val set = usage.getOrElse(logFile, Set.empty) + writer
    usage += logFile -> set
    logFile
  }

  private def release(logFile: LogFile, writer: FileWriter): Unit = {
    val set = usage.getOrElse(logFile, Set.empty) - writer
    if (set.isEmpty) {
      close(logFile)
    } else {
      usage += logFile -> set
    }
  }

  def dispose(): Unit = synchronized {
    usage.keys.foreach { logFile =>
      logFile.flush()
      logFile.dispose()
    }
    files = Map.empty
    usage = Map.empty
    current = Map.empty
  }
}