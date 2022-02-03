package scribe

import _root_.cats.effect._

object cats {
  lazy val io: ScribeEffect[IO] = apply[IO]

  implicit def _effect[F[_]](implicit sync: Sync[F]): ScribeEffect[F] = apply[F]

  def apply[F[_]: Sync]: ScribeEffect[F] = new ScribeEffectImpl[F](implicitly[Sync[F]])
}