package scribe.writer.manager

import java.nio.file.Path

case class PathResolution(path: Path, changed: Option[ChangeHandler])
