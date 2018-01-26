package scribe.writer

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.nio.file._

import scala.annotation.tailrec

case class FileWriter(directory: Path,
                      fileNameGenerator: () => String,
                      append: Boolean = true,
                      autoFlush: Boolean = true,
                      charset: Charset = Charset.defaultCharset()) extends Writer {
  private lazy val options: List[OpenOption] = if (append) {
    List(StandardOpenOption.WRITE, StandardOpenOption.APPEND, StandardOpenOption.CREATE)
  } else {
    List(StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
  }
  private var channel: Option[FileChannel] = None
  private var currentFileName: Option[String] = None

  override def write(output: String): Unit = {
    validateFileName()
    val channel = validateChannel()
    val bytes = output.getBytes(charset)
    val buffer = ByteBuffer.wrap(bytes)
    writeBuffer(buffer, channel)
    buffer.clear()
  }

  @tailrec
  private def writeBuffer(buffer: ByteBuffer, channel: FileChannel): Unit = if (buffer.hasRemaining) {
    channel.write(buffer)
    writeBuffer(buffer, channel)
  }

  protected def validateFileName(): Unit = {
    val fileName: String = fileNameGenerator()
    if (!currentFileName.contains(fileName)) {    // Changed
      channel.foreach(_.close())
      channel = None
    }
    currentFileName = Some(fileName)
  }

  protected def validateChannel(): FileChannel = channel match {
    case Some(c) => c
    case None => {
      if (!Files.exists(directory)) {       // Create the directories if it doesn't exist
        Files.createDirectories(directory)
      }
      val path = directory.resolve(currentFileName.getOrElse(throw new RuntimeException("File name cannot be empty!")))
      val c = FileChannel.open(path, options: _*)
      channel = Some(c)
      c
    }
  }

  override def dispose(): Unit = {
    super.dispose()

    channel.foreach(_.close())
  }
}

object FileWriter {
  object generator {
    def single(prefix: String = "app", suffix: String = ".log"): () => String = () => s"$prefix$suffix"

    def daily(prefix: String = "app", suffix: String = ".log"): () => String = () => {
      val l = System.currentTimeMillis()
      f"$prefix.$l%tY-$l%tm-$l%td$suffix"
    }
  }

  def single(prefix: String = "app",
             suffix: String = ".log",
             directory: Path = Paths.get("logs"),
             append: Boolean = true,
             autoFlush: Boolean = true,
             charset: Charset = Charset.defaultCharset()): FileWriter = {
    new FileWriter(directory, generator.single(prefix, suffix), append, autoFlush, charset)
  }

  def daily(prefix: String = "app",
            suffix: String = ".log",
            directory: Path = Paths.get("logs"),
            append: Boolean = true,
            autoFlush: Boolean = true,
            charset: Charset = Charset.defaultCharset()): FileWriter = {
    new FileWriter(directory, generator.daily(prefix, suffix), append, autoFlush, charset)
  }
}