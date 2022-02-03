package scribe

import _root_.cats.effect._

object cats {
  lazy val io: ScribeEffect[IO] = apply[IO]

  def apply[F[_]: Sync]: ScribeEffect[F] = ScribeEffect[F]
}