package scribe.writer.file

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.{OpenOption, StandardOpenOption}

import scala.annotation.tailrec

class NIOLogFileWriter(lf: LogFile) extends LogFileWriter {
  private lazy val options: List[OpenOption] = if (lf.append) {
    List(StandardOpenOption.WRITE, StandardOpenOption.APPEND, StandardOpenOption.CREATE)
  } else {
    List(StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
  }
  private lazy val channel: FileChannel = FileChannel.open(lf.path, options: _*)

  override def write(output: String): Unit = {
    val o = Option(output) match {
      case Some(o) => o
      case None => "null"
    }
    val bytes = o.getBytes(lf.charset)
    val buffer = ByteBuffer.wrap(bytes)
    writeBuffer(buffer)
    buffer.clear()
  }

  @tailrec
  private def writeBuffer(buffer: ByteBuffer): Unit = if (buffer.hasRemaining) {
    channel.write(buffer)
    writeBuffer(buffer)
  }

  override def flush(): Unit = channel.force(false)

  override def dispose(): Unit = if (channel.isOpen) {
    channel.close()
  }
}
