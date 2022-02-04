package scribe

import _root_.cats.effect.Sync
import sourcecode.{FileName, Line, Name, Pkg}

class LoggerWrapper[F[_]](val wrapped: Logger, val sync: Sync[F]) extends Scribe[F] {
  override def log[M](record: LogRecord[M]): F[Unit] = sync.delay(wrapped.log(record))

  override def log[M: Loggable](level: Level, message: => M, throwable: Option[Throwable])
                               (implicit pkg: Pkg, fileName: FileName, name: Name, line: Line): F[Unit] = {
    sync.defer(super.log(level, message, throwable)(implicitly[Loggable[M]], pkg, fileName, name, line))
  }
}
