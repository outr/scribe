package com.outr.scribe.writer

import java.io.{FileOutputStream, BufferedOutputStream, File}
import java.util.concurrent.atomic.AtomicInteger

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
