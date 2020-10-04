package scribe.writer.file

import java.nio.file.{Path, Paths}
import perfolation._

object LogPath {
  lazy val default: Long => Path = simple()

  def simple(name: => String = "app.log", directory: Path = Paths.get("logs")): Long => Path = {
    _ => directory.resolve(name)
  }

  def daily(prefix: => String = "app",
            separator: String = "-",
            extension: String = "log",
            directory: => Path = Paths.get("logs")): Long => Path = {
    apply(prefix, separator, (l: Long) => s"${l.t.Y}$separator${l.t.m}$separator${l.t.d}", extension, directory)
  }

  def apply(prefix: => String = "app",
            separator: String = "-",
            distinction: Long => String,
            extension: String = "log",
            directory: => Path = Paths.get("logs")): Long => Path = {
    l: Long => directory.resolve(s"$prefix$separator${distinction(l)}.$extension")
  }
}