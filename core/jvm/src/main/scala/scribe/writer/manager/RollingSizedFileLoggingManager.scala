package scribe.writer.manager

import scribe.handler._
import java.nio.file.{Files, Path}
import java.util.concurrent.atomic.AtomicLong

import scribe.LogRecord

import scala.annotation.tailrec

class RollingSizedFileLoggingManager(directory: Path,
                                     current: String,
                                     archive: => String,
                                     maxSizeInBytes: Long) extends FileLoggingManager {
  private val path: Path = directory.resolve(current)
  private lazy val size: AtomicLong = {
    val initialSize = if (Files.exists(path)) {
      Files.size(path)
    } else {
      0L
    }
    new AtomicLong(initialSize)
  }

  override def derivePath: PathResolution = if (size.setIfCondition(_ >= maxSizeInBytes, 0L)) {
    val changed = new ChangeHandler {
      override def change(): Unit = {
        val archivePath = directory.resolve(archive)
        if (Files.exists(archivePath)) {
          Files.delete(archivePath)
        }
        Files.move(path, archivePath)
      }
    }
    PathResolution(path, Some(changed))
  } else {
    PathResolution(path, None)
  }

  override def written[M](record: LogRecord[M], output: String): Unit = {
    size.addAndGet(output.length)
  }
}

object RollingSizedFileLoggingManager {
  def simple(directory: Path, current: String, maxSizeInBytes: Long): RollingSizedFileLoggingManager = {
    new RollingSizedFileLoggingManager(
      directory = directory,
      current = current,
      archive = nextIncrement(directory, current),
      maxSizeInBytes = maxSizeInBytes
    )
  }

  @tailrec
  final def nextIncrement(directory: Path, current: String, increment: Int = 1): String = {
    val fileName = s"$current.$increment"
    if (!Files.exists(directory.resolve(fileName))) {
      fileName
    } else {
      nextIncrement(directory, current, increment + 1)
    }
  }
}