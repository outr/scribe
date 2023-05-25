package scribe

import _root_.cats.effect._
import perfolation._

object cats {
  lazy val io: Scribe[IO] = apply[IO]

  implicit def effect[F[_]](implicit sync: Sync[F]): Scribe[F] = apply[F]
  implicit class LoggerExtras(val logger: Logger) extends AnyVal {
    def f[F[_]](implicit sync: Sync[F]): Scribe[F] = new LoggerWrapper[F](logger, sync)
  }
  implicit class IOExtras[Return](val io: IO[Return]) extends AnyVal {
    def timed(label: String)(implicit timer: Timer): IO[Return] = timer.chain(io, label)
  }

  def timer[Return](f: Timer => IO[Return]): IO[Return] = for {
    timer <- IO.blocking(Timer(System.currentTimeMillis()))
    r <- f(timer)
  } yield r

  def apply[F[_]: Sync]: Scribe[F] = new ScribeImpl[F](implicitly[Sync[F]])

  case class Timer(start: Long) { self =>
    private var last: Long = start

    def log(label: String): IO[Unit] = for {
      now <- IO.blocking(System.currentTimeMillis())
      elapsed = ((now - start) / 1000.0).f(f = 3)
      previous = ((now - last) / 1000.0).f(f = 3)
      _ <- io.info(s"$label (Elapsed: $elapsed seconds, Since Previous: $previous seconds)")
      _ <- IO.blocking {
        self.synchronized {
          last = now
        }
      }
    } yield ()

    def chain[Return](prev: IO[Return], label: String): IO[Return] = prev.flatMap { r =>
      log(label).map(_ => r)
    }
  }
}