package scribe

import _root_.cats.effect._
import scribe.mdc.MDC
import scribe.message.LoggableMessage
import sourcecode.{FileName, Line, Name, Pkg}

class ScribeImpl[F[_]](val sync: Sync[F]) extends AnyVal with Scribe[F] {
  override def log(record: => LogRecord): F[Unit] = sync.delay(Logger(record.className).log(record))

  override def log(level: Level, mdc: MDC, features: LogFeature*)
                               (implicit pkg: Pkg, fileName: FileName, name: Name, line: Line): F[Unit] =
    sync.defer(log(LoggerSupport(level, Nil, pkg, fileName, name, line, mdc).withFeatures(features: _*)))
}