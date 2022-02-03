package scribe

import _root_.cats.effect._
import sourcecode.{FileName, Line, Name, Pkg}

class ScribeEffectImpl[F[_]](val sync: Sync[F]) extends AnyVal with ScribeEffect[F] {
  override def info[M: Loggable](message: => M)
                                (implicit pkg: Pkg, fileName: FileName, name: Name, line: Line): F[Unit] =
    sync.delay(scribe.info(message)(implicitly[Loggable[M]], pkg, fileName, name, line))
}
