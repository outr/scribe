package scribe.writer.file

import java.io._
import java.nio.charset.Charset
import java.nio.file.{Files, Path}
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import scala.annotation.tailrec
import scala.util.Try

object LogFile {
  val AsynchronousFlushDelay: Long = 1000L
  val BufferSize: Int = 1024

  private[file] var map = Map.empty[String, LogFile]

  def apply(path: Path,
            append: Boolean = true,
            autoFlush: Boolean = true,
            charset: Charset = Charset.defaultCharset(),
            mode: LogFileMode = LogFileMode.IO): LogFile = synchronized {
    val key = s"${mode.key}.${path.normalize().toFile.getCanonicalPath}"
    map.get(key) match {
      case Some(lf) if append != lf.append || autoFlush != lf.autoFlush || charset != lf.charset => {
        lf.dispose()

        val nlf = new LogFile(key, path, append, autoFlush, charset, mode)
        map += key -> nlf
        nlf
      }
      case Some(lf) => lf
      case None => {
        val lf = new LogFile(key, path, append, autoFlush, charset, mode)
        map += key -> lf
        lf
      }
    }
  }
}

class LogFile(val key: String,
              val path: Path,
              val append: Boolean,
              val autoFlush: Boolean,
              val charset: Charset,
              val mode: LogFileMode) {
  private lazy val sizeCounter = new AtomicLong(0L)
  @volatile private var active: Boolean = false
  private lazy val writer = {
    active = true
    Option(path.getParent).foreach(Files.createDirectories(_))
    if (Files.exists(path)) {
      sizeCounter.set(Files.size(path))
    }
    mode.createWriter(this)
  }
  private lazy val flusher = new AsynchronousFlusher(this, LogFile.AsynchronousFlushDelay)
  @volatile private var disposed = false

  def size: Long = sizeCounter.get()

  def differentPath(that: LogFile): Boolean = scribe.writer.FileWriter.differentPath(this.path, that.path)

  def samePath(that: LogFile): Boolean = scribe.writer.FileWriter.samePath(this.path, that.path)

  final def write(output: String): Unit = {
    writer.write(output)
    if (autoFlush) {
      writer.flush()
    } else {
      flusher.written()
    }
    sizeCounter.addAndGet(output.length)
  }

  def replace(path: Path = path,
              append: Boolean = append,
              autoFlush: Boolean = autoFlush,
              charset: Charset = charset,
              mode: LogFileMode = mode): LogFile = {
    if (isDisposed ||
        this.path != path ||
        this.append != append ||
        this.autoFlush != autoFlush ||
        this.charset != charset ||
        this.mode != mode) {
      LogFile(path, append, autoFlush, charset, mode)
    } else {
      this
    }
  }

  def rename(fileName: String): LogFile = rename(path.getParent.resolve(fileName))

  def rename(newPath: Path): LogFile = {
    if (Files.exists(newPath)) {
      Files.delete(newPath)
    }
    if (Files.exists(path)) {
      dispose()
      Files.move(path, newPath)
    }
    LogFile(newPath, append, autoFlush, charset, mode)
  }

  final def gzip(destination: String = s"${path.getFileName.toString}.gz",
                 deleteOriginal: Boolean = true): Unit = {
    flush()
    dispose()
    val buffer = new Array[Byte](LogFile.BufferSize)
    val file = path.toAbsolutePath.toFile
    val outputFile = new File(file.getParentFile, destination)
    val input = new FileInputStream(file)
    val output = new GZIPOutputStream(new FileOutputStream(outputFile))
    try {
      stream(input, output, buffer)
      output.flush()
    } finally {
      Try(input.close())
      Try(output.close())
      if (deleteOriginal) {
        if (!file.delete()) {
          file.deleteOnExit()
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

  def isActive: Boolean = active && !isDisposed

  def isDisposed: Boolean = disposed

  def flush(): Unit = writer.flush()

  def dispose(): Unit = {
    disposed = true
    LogFile.synchronized {
      LogFile.map -= key
    }
    if (isActive) {
      try {
        writer.dispose()
      } catch {
        case _: Throwable => // Ignore
      }
    }
  }

  def delete(): Boolean = {
    dispose()
    Files.deleteIfExists(path)
  }
}