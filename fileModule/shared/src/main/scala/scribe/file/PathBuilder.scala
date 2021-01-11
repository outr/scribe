package scribe.file

import java.nio.file.{Path, Paths}

case class PathBuilder(parts: List[PathPart]) extends PathList {
  def before(writer: FileWriter): Unit = parts.foreach(_.before(writer))
  def after(writer: FileWriter): Unit = parts.foreach(_.after(writer))

  def path(timeStamp: Long): Path = parts.foldLeft(PathBuilder.DefaultPath)((previous, part) => part.current(previous, timeStamp))

  override def list(): List[Path] = parts.foldLeft(List(PathBuilder.DefaultPath))((previous, part) => previous.flatMap(part.all))

  def /(part: PathPart): PathBuilder = copy(parts ::: List(part))
}

object PathBuilder {
  lazy val DefaultPath: Path = Paths.get("logs", "app.log")
  lazy val Default: PathBuilder = PathBuilder(List(PathPart.SetPath(DefaultPath)))

  def static(path: Path): PathBuilder = PathBuilder(List(PathPart.SetPath(path)))
}

trait PathList {
  def list(): List[Path]
}