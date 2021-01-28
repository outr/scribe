package scribe.file.path

import scribe.file.{FileWriter, LogFile}
import scribe.util.Time

import java.nio.file.{Files, Path}

case class MaxSize(maxSizeInBytes: Long, separator: String) extends FileNamePart {
  private val threadLocal = new ThreadLocal[Int] {
    override def initialValue(): Int = 0
  }

  override def current(timeStamp: Long): String = {
    val i = threadLocal.get()
    if (i == 0) {
      ""
    } else {
      s"$separator$i"
    }
  }

  override def regex: String = s"([$separator]\\d*)?"

  override def before(writer: FileWriter): Unit = {
    val logFile = LogFile(writer)
    if (logFile.size >= maxSizeInBytes && Files.exists(logFile.path)) {
      val path = pathFor(writer, 1)
      val lastModified = Files.getLastModifiedTime(logFile.path)
      rollPaths(writer)
      LogFile.move(logFile, path)
      Files.setLastModifiedTime(path, lastModified)
    }
  }

  private def rollPaths(writer: FileWriter, i: Int = 1): Unit = {
    val path = pathFor(writer, i)
    if (Files.exists(path)) {
      rollPaths(writer, i + 1)
      val nextPath = pathFor(writer, i + 1)
      val lastModified = Files.getLastModifiedTime(path)
      LogFile.copy(path, nextPath)
      LogFile.truncate(path)
      Files.setLastModifiedTime(nextPath, lastModified)
    }
  }

  private def pathFor(writer: FileWriter, i: Int): Path = {
    threadLocal.set(i)
    try {
      writer.pathBuilder.path(Time())
    } finally {
      threadLocal.remove()
    }
  }
}
object MaxSize {
  val OneHundredMeg: Long = 100000000L
}