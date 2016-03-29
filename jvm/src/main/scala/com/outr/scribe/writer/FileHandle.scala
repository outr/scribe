package com.outr.scribe.writer

import java.io.File
import java.nio.file.{Files, StandardOpenOption}
import java.util.concurrent.atomic.AtomicInteger

class FileHandle(val file: File, append: Boolean) {
  val references = new AtomicInteger(0)

  // Make sure the directories exist
  file.getParentFile.mkdirs()

  private val writer = Files.newBufferedWriter(
    file.toPath,
    if (append) StandardOpenOption.APPEND else StandardOpenOption.TRUNCATE_EXISTING,
    StandardOpenOption.CREATE,
    StandardOpenOption.WRITE
  )

  def write(s: String, autoFlush: Boolean): Unit = {
    writer.write(s)
    if (autoFlush) {
      writer.flush()
    }
  }

  def close(): Unit = {
    writer.flush()
    writer.close()
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
