package scribe

trait Scribe[F[_]] extends Any with LoggerSupport[F[Unit]]

object Scribe {
  def apply[F[_]: Scribe]: Scribe[F] = implicitly[Scribe[F]]
}