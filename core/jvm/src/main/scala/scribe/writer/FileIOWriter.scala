package scribe.writer

import java.io
import java.io.PrintWriter
import java.nio.charset.Charset
import java.nio.file._

import scribe.writer.manager.FileLoggingManager

case class FileIOWriter(manager: FileLoggingManager,
                        append: Boolean = true,
                        autoFlush: Boolean = false,
                        charset: Charset = Charset.defaultCharset()) extends FileWriter {
  private var path: Option[Path] = None
  private var writer: Option[PrintWriter] = None

  override def write(output: String): Unit = {
    val writer = validate()
    writer.write(output)
    if (autoFlush) flush()
  }

  override def flush(): Unit = writer.foreach(_.flush())

  protected def validate(): PrintWriter = synchronized {
    val resolution = manager.derivePath
    if (resolution.changed.nonEmpty || !path.exists(Files.isSameFile(_, resolution.path))) {
      // Set the new path
      this.path = Some(resolution.path)
      // Close and cleanup active PrintWriter
      writer.foreach { w =>
        w.flush()
        w.close()
      }
      writer = None
      // Apply the manager's change if provided
      resolution.changed.foreach(_.change())
      // Re-open the channel
      if (!Files.exists(resolution.path.getParent)) {
        Files.createDirectories(resolution.path.getParent)
      }
      val file = resolution.path.toFile
      writer = Some(new PrintWriter(new io.FileWriter(file, append)))
    }
    writer.getOrElse(throw new RuntimeException("No PrintWriter created during validate!"))
  }

  override def dispose(): Unit = {
    super.dispose()

    writer.foreach(_.close())
  }
}