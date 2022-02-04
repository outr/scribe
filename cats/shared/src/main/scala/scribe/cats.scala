package scribe

import _root_.cats.effect._

object cats {
  lazy val io: Scribe[IO] = apply[IO]

  implicit def effect[F[_]](implicit sync: Sync[F]): Scribe[F] = apply[F]
  implicit class LoggerExtras(val logger: Logger) extends AnyVal {
    def f[F[_]](implicit sync: Sync[F]): Scribe[F] = new LoggerWrapper[F](logger, sync)
  }

  def apply[F[_]: Sync]: Scribe[F] = new ScribeImpl[F](implicitly[Sync[F]])
}