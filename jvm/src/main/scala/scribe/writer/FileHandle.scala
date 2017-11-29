package scribe.writer

import java.nio.file.Path
import java.nio.file.{Files, StandardOpenOption}
import java.util.concurrent.atomic.AtomicInteger

class FileHandle(val file: Path, append: Boolean) {
  val references = new AtomicInteger(0)

  // Make sure the directories exist
  for (parent <- Option(file.getParent)) {
    Files.createDirectories(parent)
  }

  private val writer = Files.newBufferedWriter(
    file,
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
  @volatile private var map = Map.empty[File, FileHandle]

  def apply(file: Path, append: Boolean): FileHandle = synchronized {
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
    if (handle.references.decrementAndGet() < 1) {
      map -= handle.file
      handle.close()
    }
  }
}
