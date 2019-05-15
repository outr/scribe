package scribe.writer.action

import java.nio.file.{Files, Path}

import scribe.util.Time
import scribe.writer.FileWriter
import scribe.writer.file.LogFile

import scala.concurrent.duration.FiniteDuration

case class PathResolvingAction(path: Long => Path, gzip: Boolean, checkRate: FiniteDuration) extends Action {
  @volatile private var currentFileStamp: Long = 0L

  override def apply(previous: LogFile, current: LogFile): LogFile = rateDelayed(checkRate, current) {
    if ((current != previous && previous.differentPath(current)) || currentFileStamp == 0L) {
      if (Files.exists(current.path)) {
        currentFileStamp = Files.getLastModifiedTime(current.path).toMillis
      }
    }
    val now = Time()
    val currentPath = path(currentFileStamp)
    val nowPath = path(now)
    currentFileStamp = now
    if (FileWriter.differentPath(currentPath, nowPath) && Files.exists(current.path)) {
      val renamed = current.rename(currentPath)
      if (gzip) {
        renamed.gzip()
      }
      current.replace()
    } else {
      current
    }
  }
}