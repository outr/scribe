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
      }
      current
    } else {
      val previousPath = path(currentFileStamp)
      val currentPath = path(Time())
      if (FileWriter.differentPath(previousPath, currentPath)) {
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
}
