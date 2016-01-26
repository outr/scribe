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

  protected def checkOutput() = synchronized {
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

  def Daily(name: String = "application"): () => String = DatePattern(name + ".%1$tY-%1$tm-%1$td.log")
}

class FileHandle(val file: File, append: Boolean) {
  val references = new AtomicInteger(0)

  // Make sure the directories exist
  file.getParentFile.mkdirs()

  private val output = new BufferedOutputStream(new FileOutputStream(file, append))

  def write(s: String, autoFlush: Boolean): Unit = {
    output.write(s.getBytes)
    if (autoFlush) {
      output.flush()
    }
  }

  def close(): Unit = {
    output.flush()
    output.close()
  }
}

object FileHandle {
  private var map = Map.empty[File, FileHandle]

  def apply(file: File, append: Boolean): FileHandle = synchronized {
    val h = map.get(file) match {
      case Some(handle) => handle
      case None => {
        val handle = new FileHandle(file, append)
        map += file -> handle
        handle
      }
    }
    h.references.incrementAndGet()
    h
  }

  def release(handle: FileHandle): Unit = synchronized {
    handle.references.decrementAndGet()
    if (handle.references.get() == 0) {
      map -= handle.file
      handle.close()
    }
  }
}