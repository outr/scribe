package scribe.file.path

import scribe.file.{FileWriter, LogFile}
import scribe.util.Time

import java.io.File

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
    if (logFile.size >= maxSizeInBytes && logFile.file.exists()) {
      val path = fileFor(writer, 1)
      val lastModified = logFile.file.lastModified()
      rollPaths(writer)
      LogFile.move(logFile, path)
      path.setLastModified(lastModified)
    }
  }

  private def rollPaths(writer: FileWriter, i: Int = 1): Unit = {
    val path = fileFor(writer, i)
    if (path.exists()) {
      rollPaths(writer, i + 1)
      val nextPath = fileFor(writer, i + 1)
      val lastModified = path.lastModified()
      LogFile.copy(path, nextPath)
      LogFile.truncate(path)
      nextPath.setLastModified(lastModified)
    }
  }

  private def fileFor(writer: FileWriter, i: Int): File = {
    threadLocal.set(i)
    try {
      writer.pathBuilder.file(Time())
    } finally {
      threadLocal.remove()
    }
  }
}
object MaxSize {
  val OneHundredMeg: Long = 100000000L
}