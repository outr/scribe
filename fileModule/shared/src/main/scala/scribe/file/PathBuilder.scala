package scribe.file

import java.nio.file.{Path, Paths}

case class PathBuilder(parts: List[PathPart]) {
  def path: Path = parts.foldLeft(PathBuilder.DefaultPath)((previous, part) => part.current(previous))

  def list(): List[Path] = parts.foldLeft(List(PathBuilder.DefaultPath))((previous, part) => previous.flatMap(part.all))
}

object PathBuilder {
  lazy val DefaultPath: Path = Paths.get("logs", "app.log")
  lazy val Default: PathBuilder = PathBuilder(List(PathPart.SetPath(DefaultPath)))

  def static(path: Path): PathBuilder = PathBuilder(List(PathPart.SetPath(path)))
}