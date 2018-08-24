package scribe.writer.action

import java.nio.file.{Files, Path}

import scribe.writer.file.LogFile

case class MaxLogFilesAction(max: Int,
                             lister: Path => List[Path],
                             checkRate: Long) extends Action {
  override def apply(previous: LogFile, current: LogFile): LogFile = rateDelayed(checkRate, current) {
    val logs = lister(current.path)
    if (logs.length > max) {
      logs.take(logs.length - max).foreach { path =>
        Files.deleteIfExists(path)
      }
    }
    current
  }
}

object MaxLogFilesAction {
  val MatchLogAndGZ: Path => Boolean = (path: Path) => {
    val name = path.toString.toLowerCase
    name.endsWith(".log") || name.endsWith(".log.gz")
  }

  val MatchLogAndGZInSameDirectory: Path => List[Path] = (path: Path) => {
    val directory = Option(path.toAbsolutePath.getParent)
      .getOrElse(throw new RuntimeException(s"No parent found for ${path.toAbsolutePath.toString}"))
    Files.list(directory).iterator().asScala.filter(MatchLogAndGZ).toList.sortBy(Files.getLastModifiedTime(_)).reverse
  }
}