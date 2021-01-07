package scribe.file

import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._

trait PathPart {
  def current(previous: Path): Path

  def all(previous: Path): List[Path]
}

object PathPart {
  case object Root extends PathPart {
    override def current(previous: Path): Path = Paths.get("/")

    override def all(previous: Path): List[Path] = List(current(previous))
  }

  case class SetPath(path: Path) extends PathPart {
    override def current(previous: Path): Path = path

    override def all(previous: Path): List[Path] = List(path)
  }

  case class Static(part: String) extends PathPart {
    override def current(previous: Path): Path = previous.resolve(part)

    override def all(previous: Path): List[Path] = List(current(previous))
  }

  case class Matcher(matcher: Path => Boolean, apply: Path => Path) extends PathPart {
    override def current(previous: Path): Path = apply(previous)

    override def all(previous: Path): List[Path] = Files.list(previous).iterator().asScala.toList.filter(matcher)
  }
}