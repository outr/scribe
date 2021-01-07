package scribe

import scribe.file.PathPart.FileName

import java.nio.file.{Files, Path}
import scala.language.implicitConversions
import perfolation._
import scribe.util.Time

package object file {
  implicit def path2PathBuilder(path: Path): PathBuilder = PathBuilder(List(PathPart.SetPath(path)))
  implicit def string2FileName(s: String): FileName = FileName(List(FileNamePart.Static(s)))
  implicit def string2FileNamePart(s: String): FileNamePart = FileNamePart.Static(s)

  def day: FileNamePart = FileNamePart.Day
  def month: FileNamePart = FileNamePart.Month
  def year: FileNamePart = FileNamePart.Year

  def daily: Path => Boolean = (path: Path) => if (Files.exists(path)) {
    val lastModified = Files.getLastModifiedTime(path).toMillis
    val lastModifiedDay = lastModified.t.dayOfYear
    val currentDay = Time().t.dayOfYear
    lastModifiedDay != currentDay
  } else {
    false
  }
}