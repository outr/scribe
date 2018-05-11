package scribe.writer

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.nio.file._

import scribe.LogRecord
import scribe.writer.manager.FileLoggingManager

import scala.annotation.tailrec

case class FileNIOWriter(manager: FileLoggingManager,
                         append: Boolean = true,
                         autoFlush: Boolean = false,
                         charset: Charset = Charset.defaultCharset()) extends FileWriter {
  private lazy val options: List[OpenOption] = if (append) {
    List(StandardOpenOption.WRITE, StandardOpenOption.APPEND, StandardOpenOption.CREATE)
  } else {
    List(StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
  }
  private var path: Option[Path] = None
  private var channel: Option[FileChannel] = None

  override def write[M](record: LogRecord[M], output: String): Unit = {
    val channel = validate()
    val bytes = output.getBytes(charset)
    val buffer = ByteBuffer.wrap(bytes)
    writeBuffer(buffer, channel)
    buffer.clear()
    if (autoFlush) flush()
  }

  override def flush(): Unit = channel.foreach(_.force(false))

  @tailrec
  private def writeBuffer(buffer: ByteBuffer, channel: FileChannel): Unit = if (buffer.hasRemaining) {
    channel.write(buffer)
    writeBuffer(buffer, channel)
  }

  protected def validate(): FileChannel = synchronized {
    val resolution = manager.derivePath

    FileWriter.validate(path, resolution).foreach { p =>
      // Set the new path
      this.path = Some(p)
      // Close and cleanup active channel
      channel.foreach(_.close())
      channel = None
      // Apply the manager's change if provided
      resolution.changed.foreach(_.change())
      // Re-open the channel
      if (!Files.exists(p.getParent)) {
        Files.createDirectories(p.getParent)
      }
      channel = Some(FileChannel.open(p, options: _*))
    }
    channel.getOrElse(throw new RuntimeException("No channel created during validate!"))
  }

  override def dispose(): Unit = {
    super.dispose()

    channel.foreach(_.close())
  }
}