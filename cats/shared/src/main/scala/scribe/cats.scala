package scribe

import _root_.cats.effect._

object cats {
  lazy val io: Scribe[IO] = apply[IO]

  implicit def effect[F[_]](implicit sync: Sync[F]): Scribe[F] = apply[F]

  def apply[F[_]: Sync]: Scribe[F] = new ScribeImpl[F](implicitly[Sync[F]])
}