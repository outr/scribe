package scribe

import _root_.cats.effect._

object cats {
  def apply[F[_]: Sync]: ScribeEffect[F] = ScribeEffect[F]
}