package scribe.writer

import java.io
import java.io.PrintWriter
import java.nio.charset.Charset
import java.nio.file._

case class FileIOWriter(directory: Path,
                      fileNameGenerator: () => String,
                      append: Boolean = true,
                      autoFlush: Boolean = false,
                      charset: Charset = Charset.defaultCharset()) extends FileWriter {
  private var writer: Option[PrintWriter] = None
  private var currentFileName: Option[String] = None

  override def write(output: String): Unit = {
    validateFileName()
    val writer = validateWriter()
    writer.write(output)
    if (autoFlush) flush()
  }

  override def flush(): Unit = writer.foreach(_.flush())

  protected def validateFileName(): Unit = {
    val fileName: String = fileNameGenerator()
    if (!currentFileName.contains(fileName)) {    // Changed
      writer.foreach { w =>
        w.flush()
        w.close()
      }
      writer = None
    }
    currentFileName = Some(fileName)
  }

  protected def validateWriter(): PrintWriter = writer match {
    case Some(w) => w
    case None => {
      if (!Files.exists(directory)) {       // Create the directories if it doesn't exist
        Files.createDirectories(directory)
      }
      val path = directory.resolve(currentFileName.getOrElse(throw new RuntimeException("File name cannot be empty!")))
      val file = path.toFile
      val w = new PrintWriter(new io.FileWriter(file, append))
      writer = Some(w)
      w
    }
  }

  override def dispose(): Unit = {
    super.dispose()

    writer.foreach(_.close())
  }
}

object FileIOWriter {
  def single(prefix: String = "app",
             suffix: String = ".log",
             directory: Path = Paths.get("logs"),
             append: Boolean = true,
             autoFlush: Boolean = false,
             charset: Charset = Charset.defaultCharset()): FileIOWriter = {
    new FileIOWriter(directory, FileWriter.generator.single(prefix, suffix), append, autoFlush, charset)
  }

  def daily(prefix: String = "app",
            suffix: String = ".log",
            directory: Path = Paths.get("logs"),
            append: Boolean = true,
            autoFlush: Boolean = false,
            charset: Charset = Charset.defaultCharset()): FileIOWriter = {
    new FileIOWriter(directory, FileWriter.generator.daily(prefix, suffix), append, autoFlush, charset)
  }
}
