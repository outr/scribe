package scribe.writer.manager

import java.nio.file.Path

case class FormattedFileLoggingManager(directory: Path, fileName: Long => String) extends FileLoggingManager {
  override def derivePath: PathResolution = {
    val path = directory.resolve(fileName(System.currentTimeMillis()))
    PathResolution(path, None)
  }
}
