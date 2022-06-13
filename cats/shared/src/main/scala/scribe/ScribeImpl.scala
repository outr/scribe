package scribe

import _root_.cats.effect._
import scribe.message.LoggableMessage
import sourcecode.{FileName, Line, Name, Pkg}

class ScribeImpl[F[_]](val sync: Sync[F]) extends AnyVal with Scribe[F] {
  override def log(record: LogRecord): F[Unit] = sync.delay(Logger(record.className).log(record))

  override def log(level: Level, messages: LoggableMessage*)
                               (implicit pkg: Pkg, fileName: FileName, name: Name, line: Line): F[Unit] =
    sync.defer(log(LoggerSupport(level, messages.toList, pkg, fileName, name, line)))
}

