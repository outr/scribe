package scribe.writer.action

import java.nio.file.{Files, Path}

import scribe.util.Time
import scribe.writer.FileWriter
import scribe.writer.file.LogFile

import scala.concurrent.duration.FiniteDuration

case class PathResolvingAction(path: Long => Path, gzip: Boolean, checkRate: FiniteDuration) extends Action {
  @volatile private var currentFileStamp: Long = Time()

  override def apply(previous: LogFile, current: LogFile): LogFile = rateDelayed(checkRate, current) {
    if ((current != previous && previous.differentPath(current)) || currentFileStamp == 0L) {
      if (Files.exists(current.path)) {
        currentFileStamp = Files.getLastModifiedTime(current.path).toMillis
      }
      current
    } else {
      val previousPath = path(currentFileStamp)
      val now = Time()
      val currentPath = path(now)
      if (FileWriter.differentPath(previousPath, currentPath)) {
        val renamed = current.rename(previousPath)
        if (gzip) {
          renamed.gzip()
        }
        currentFileStamp = now
        current.replace()
      } else {
        current
      }
    }
  }
}
