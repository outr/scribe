package scribe.file

import scribe.file.path.PathPart

import java.io.File
import java.nio.file.Path

case class PathBuilder(parts: List[PathPart]) {
  def before(writer: FileWriter): Unit = parts.foreach(_.before(writer))
  def after(writer: FileWriter): Unit = parts.foreach(_.after(writer))

  def file(timeStamp: Long): File = {
    val path = parts.foldLeft(PathBuilder.DefaultPath)((previous, part) => part.current(previous, timeStamp))
    new File(path)
  }

  def iterator(): Iterator[File] = parts
    .foldLeft(Iterator(PathBuilder.DefaultPath))((previous, part) => previous.flatMap(part.all))
    .map(new File(_))

  def /(part: PathPart): PathBuilder = copy(parts ::: List(part))
}

object PathBuilder {
  lazy val DefaultPath: String = new File("logs", "app.log").getAbsolutePath
  lazy val Default: PathBuilder = PathBuilder(List(PathPart.SetPath(DefaultPath)))

  def static(path: Path): PathBuilder = PathBuilder(List(PathPart.SetPath(path.toString)))
}