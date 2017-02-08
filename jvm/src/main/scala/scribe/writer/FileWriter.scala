package scribe.writer

import java.io.{BufferedOutputStream, File, FileOutputStream}
import java.util.concurrent.atomic.AtomicInteger

import scribe.LogRecord
import scribe.formatter.Formatter

class FileWriter(val directory: File,
                 val filenameGenerator: () => String,
                 val append: Boolean = true,
                 val autoFlush: Boolean = true) extends Writer {
  private var currentFilename: String = _
  private var handle: Option[FileHandle] = None

  protected def checkOutput(): Unit = synchronized {
    val filename = filenameGenerator()
    handle match {
      case Some(h) if currentFilename != filename => {
        FileHandle.release(h)
        currentFilename = filename
        handle = Some(FileHandle(new File(directory, currentFilename), append))
      }
      case None => {
        currentFilename = filename
        handle = Some(FileHandle(new File(directory, currentFilename), append))
      }
      case _ => // Ignore
    }
  }

  def write(record: LogRecord, formatter: Formatter): Unit = {
    checkOutput()
    handle.foreach(_.write(formatter.format(record), autoFlush))
  }

  def close(): Unit = handle.foreach(FileHandle.release)
}

object FileWriter {
  def datePattern(pattern: String): () => String = () => pattern.format(System.currentTimeMillis())

  def daily(name: String = "application",
            directory: File = new File("logs"),
            append: Boolean = true,
            autoFlush: Boolean = true): FileWriter = {
    new FileWriter(directory, datePattern(name + ".%1$tY-%1$tm-%1$td.log"), append, autoFlush)
  }
  def flat(name: String = "application",
           directory: File = new File("logs"),
           append: Boolean = true,
           autoFlush: Boolean = true): FileWriter = new FileWriter(directory, () => s"$name.log", append, autoFlush)
}
