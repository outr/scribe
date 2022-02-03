package scribe

import _root_.cats.effect._

trait ScribeEffect[F[_]] extends Any {
  def info[M: Loggable](message: => M)
                       (implicit pkg: sourcecode.Pkg,
                        fileName: sourcecode.FileName,
                        name: sourcecode.Name,
                        line: sourcecode.Line): F[Unit]
}

object ScribeEffect {
  def apply[F[_]](implicit sync: Sync[F]): ScribeEffect[F] = new ScribeEffectImpl[F](sync)
}