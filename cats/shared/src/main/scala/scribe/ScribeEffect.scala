package scribe

trait ScribeEffect[F[_]] extends Any with LoggerSupport[F[Unit]]

object ScribeEffect {
  def apply[F[_]: ScribeEffect]: ScribeEffect[F] = implicitly[ScribeEffect[F]]
}