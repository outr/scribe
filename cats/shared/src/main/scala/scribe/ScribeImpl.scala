package scribe

import _root_.cats.effect._
import sourcecode.{FileName, Line, Name, Pkg}

class ScribeImpl[F[_]](val sync: Sync[F]) extends AnyVal with Scribe[F] {
  override def log[M](record: LogRecord[M]): F[Unit] = sync.delay(Logger(record.className).log(record))

  override def log[M: Loggable](level: Level, message: => M, throwable: Option[Throwable])
                               (implicit pkg: Pkg, fileName: FileName, name: Name, line: Line): F[Unit] = {
    sync.defer(log[M](LoggerSupport[M](level, message, throwable, implicitly[Loggable[M]], pkg, fileName, name, line)))
  }
}

