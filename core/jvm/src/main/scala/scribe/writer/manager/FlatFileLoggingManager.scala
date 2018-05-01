package scribe.writer.manager

import java.nio.file.Path

case class FlatFileLoggingManager(path: Path) extends FileLoggingManager {
  override val derivePath: PathResolution = PathResolution(path, None)
}
