package scribe.file.path

import scribe.file.FileWriter
import scribe.util.Time

import java.io.File
import java.nio.file.{Files, Path}
import scala.concurrent.duration._

case class Rolling(parts: List[FileNamePart],
                   action: (File, File) => Unit,
                   minimumValidationFrequency: FiniteDuration) extends FileNamePart {
  private lazy val partsRegex = parts.map(_.regex).mkString
  private val threadLocal = new ThreadLocal[Rolling.Mode] {
    override def initialValue(): Rolling.Mode = Rolling.Standard
  }

  override def current(timeStamp: Long): String = threadLocal.get() match {
    case Rolling.Standard | Rolling.OnlyCurrent => ""
    case Rolling.OnlyRolling => parts.map(_.current(timeStamp)).mkString
  }

  override def regex: String = threadLocal.get() match {
    case Rolling.Standard => s"($partsRegex)?"
    case Rolling.OnlyCurrent => ""
    case Rolling.OnlyRolling => partsRegex
  }

  private object nextRunFor {
    private var map: Map[FileWriter, Long] = Map.empty

    def apply(writer: FileWriter): Long = synchronized {
      map.getOrElse(writer, 0L)
    }

    def update(writer: FileWriter, nextRun: Long): Unit = synchronized {
      map += writer -> nextRun
    }
  }

  override def before(writer: FileWriter): Unit = if (Time() >= nextRunFor(writer)) {
    val currentPaths: List[File] = {
      threadLocal.set(Rolling.OnlyCurrent)
      try {
        writer.list()
      } finally {
        threadLocal.remove()
      }
    }
    val existing: File = {
      threadLocal.set(Rolling.OnlyRolling)
      try {
        writer.resolveFile()
      } finally {
        threadLocal.remove()
      }
    }

    currentPaths.foreach { cp =>
      val lastModified = cp.lastModified()
      val rp = rollingFile(lastModified, writer)
      if (rp != existing && !rp.exists()) {
        action(cp, rp)
      }
    }

    nextRunFor(writer) = (Time() + minimumValidationFrequency.toMillis :: parts.flatMap(_.nextValidation(Time()))).min
  }

  def rollingFile(timeStamp: Long, writer: FileWriter): File = {
    threadLocal.set(Rolling.OnlyRolling)
    try {
      writer.pathBuilder.file(timeStamp)
    } finally {
      threadLocal.remove()
    }
  }
}

object Rolling {
  sealed trait Mode

  case object Standard extends Mode
  case object OnlyCurrent extends Mode
  case object OnlyRolling extends Mode
}