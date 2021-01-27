package scribe.file

import scribe.file.path.PathPart

import java.nio.file.{Path, Paths}

case class PathBuilder(parts: List[PathPart]) {
  def before(writer: FileWriter): Unit = parts.foreach(_.before(writer))
  def after(writer: FileWriter): Unit = parts.foreach(_.after(writer))

  def path(timeStamp: Long): Path = Paths.get(parts.foldLeft(PathBuilder.DefaultPath)((previous, part) => part.current(previous, timeStamp)))

  def iterator(): Iterator[Path] = parts
    .foldLeft(Iterator(PathBuilder.DefaultPath))((previous, part) => previous.flatMap(part.all))
    .map(Paths.get(_))

  def /(part: PathPart): PathBuilder = copy(parts ::: List(part))
}

object PathBuilder {
  lazy val DefaultPath: String = Paths.get("logs", "app.log").toAbsolutePath.toString
  lazy val Default: PathBuilder = PathBuilder(List(PathPart.SetPath(DefaultPath)))

  def static(path: Path): PathBuilder = PathBuilder(List(PathPart.SetPath(path.toString)))
}