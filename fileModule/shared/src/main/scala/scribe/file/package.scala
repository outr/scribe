package scribe

import scribe.file.path._
import scribe.file.path.PathPart.FileName

import java.nio.file.{Path, Paths, StandardOpenOption}
import scala.language.implicitConversions

import java.nio.channels.FileChannel

package object file {
  val DefaultBufferSize: Int = 1024

  implicit def path2PathBuilder(path: Path): PathBuilder = PathBuilder(List(PathPart.SetPath(path.toAbsolutePath.toString)))
  implicit def string2PathBuilder(s: String): PathBuilder = PathBuilder(List(PathPart.SetPath(s)))
  implicit def string2FileName(s: String): FileName = FileName(List(FileNamePart.Static(s)))
  implicit def string2FileNamePart(s: String): FileNamePart = FileNamePart.Static(s)
  implicit def fileNamePart2FileName(part: FileNamePart): FileName = FileName(List(part))

  def day: FileNamePart = FileNamePart.Day
  def month: FileNamePart = FileNamePart.Month
  def year: FileNamePart = FileNamePart.Year
  def rolling(fileName: FileName, truncate: Boolean = true): FileNamePart = Rolling(fileName.parts, (logFile, path) => {
    if (truncate) {
      LogFile.copy(logFile, path)
      val fc = FileChannel.open(logFile.path, StandardOpenOption.WRITE)
      try {
        fc.truncate(0L)
      } finally {
        fc.close()
      }
    } else {
      LogFile.move(logFile, path)
    }
  })
  def rollingGZIP(fileName: FileName = string2FileName(".gz"),
                  deleteOriginal: Boolean = true,
                  bufferSize: Int = DefaultBufferSize): FileNamePart = Rolling(fileName.parts, (logFile, path) => {
    LogFile.gzip(logFile, path, deleteOriginal, bufferSize)
  })
  def maxSize(max: Long = MaxSize.OneHundredMeg, separator: String = "-"): FileNamePart = MaxSize(max, separator)
  def maxLogs(max: Int = 10): FileNamePart = MaxLogs(max)

  def daily(separator: String = "-"): FileName = year % separator % month % separator % day
}