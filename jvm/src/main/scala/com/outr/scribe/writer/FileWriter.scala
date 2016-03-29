package com.outr.scribe.writer

import java.io.{BufferedOutputStream, File, FileOutputStream}
import java.util.concurrent.atomic.AtomicInteger

import com.outr.scribe.LogRecord
import com.outr.scribe.formatter.Formatter

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
  def DatePattern(pattern: String): () => String = () => pattern.format(System.currentTimeMillis())

  def Daily(name: String = "application",
            directory: File = new File("logs"),
            append: Boolean = true,
            autoFlush: Boolean = true): FileWriter = new FileWriter(directory, Generator.Daily(name), append, autoFlush)
  def Flat(name: String = "application",
           directory: File = new File("logs"),
           append: Boolean = true,
           autoFlush: Boolean = true): FileWriter = new FileWriter(directory, () => s"$name.log", append, autoFlush)

  object Generator {
    def Daily(name: String = "application"): () => String = DatePattern(name + ".%1$tY-%1$tm-%1$td.log")
  }
}
