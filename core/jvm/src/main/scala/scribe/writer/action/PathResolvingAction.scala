package scribe.writer.action

import java.nio.file.{Files, Path}

import scribe.util.Time
import scribe.writer.FileWriter
import scribe.writer.file.LogFile

case class PathResolvingAction(path: Long => Path, gzip: Boolean, checkRate: Long) extends Action {
  @volatile private var currentFileStamp: Long = 0L

  override def apply(previous: LogFile, current: LogFile): LogFile = rateDelayed(checkRate, current) {
    if ((current != previous && previous.differentPath(current)) || currentFileStamp == 0L) {
      if (Files.exists(current.path)) {
        currentFileStamp = Files.getLastModifiedTime(current.path).toMillis
      } else {
        currentFileStamp = Time()
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
