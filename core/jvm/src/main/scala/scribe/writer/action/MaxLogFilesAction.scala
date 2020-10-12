package scribe.writer.action

import java.nio.file.{Files, Path}

import scribe.writer.file.LogFile

import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters._

case class MaxLogFilesAction(max: Int,
                             lister: LogFile => List[Path],
                             logManager: Path => Unit,
                             checkRate: FiniteDuration) extends Action {
  override def apply(previous: LogFile, current: LogFile): LogFile = rateDelayed(checkRate, current) {
    val logs = lister(current)
    val delete = logs.length - max
    if (delete > 0) {
      logs.take(delete).foreach(logManager)
    }
    current
  }
}

object MaxLogFilesAction {
  val MatchLogAndGZ: Path => Boolean = (path: Path) => {
    val name = path.toString.toLowerCase
    name.endsWith(".log") || name.endsWith(".log.gz")
  }

  val MatchLogAndGZInSameDirectory: LogFile => List[Path] = (logFile: LogFile) => {
    val path = logFile.path
    val fileName = path.getFileName.toString
    val prefix = if (fileName.contains('-')) {
      fileName.substring(0, fileName.indexOf('-'))
    } else {
      fileName.substring(0, fileName.indexOf('.'))
    }
    val directory = Option(path.toAbsolutePath.getParent)
      .getOrElse(throw new RuntimeException(s"No parent found for ${path.toAbsolutePath.toString}"))
    val stream = Files.newDirectoryStream(directory)
    try {
      stream
        .iterator()
        .asScala
        .toList
        .filter(MatchLogAndGZ)
        .filter(_.getFileName.toString.startsWith(prefix))
        .sortBy(Files.getLastModifiedTime(_))
    } finally {
      stream.close()
    }
  }
}