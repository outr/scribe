package scribe.file

import scribe.file.path.PathPart

import java.io.File
import java.nio.file.Path

/**
  * @param partsList of PathPart's which will be used to build the name of the logfile
  * @param rootDir directory in which logs will be stored
  */
case class PathBuilder(parts: List[PathPart], rootDir: File = PathBuilder.DefaultRootDir) {
  val rootDirPath = rootDir.getAbsolutePath()

  def before(writer: FileWriter): Unit = parts.foreach(_.before(writer))
  def after(writer: FileWriter): Unit = parts.foreach(_.after(writer))

  def file(timeStamp: Long): File = {
    val path = parts.foldLeft(rootDirPath)((previous, part) => part.current(previous, timeStamp))
    new File(path)
  }

  def iterator(): Iterator[File] = parts
    .foldLeft(Iterator(rootDirPath))((previous, part) => previous.flatMap(part.all))
    .map(new File(_))

  def /(part: PathPart): PathBuilder = copy(parts ::: List(part))
}

object PathBuilder {
  lazy val DefaultRootDir: File = new File("logs", "app.log")
  lazy val DefaultPath: String = DefaultRootDir.getAbsolutePath
  lazy val Default: PathBuilder = PathBuilder(List(PathPart.SetPath(DefaultPath)))

  def static(path: Path): PathBuilder = PathBuilder(List(PathPart.SetPath(path.toString)))
  def static(fileName: Path, rootDir: Path): PathBuilder = PathBuilder(List(PathPart.SetPath(fileName.toString)), rootDir.toFile())
  def static(parts: List[PathPart], rootDir: Path): PathBuilder = PathBuilder(parts, rootDir.toFile())
}
