package scribe

import _root_.cats._
import _root_.cats.effect._
import _root_.cats.syntax.all._

object Test extends IOApp {
//  override def run(args: List[String]): IO[ExitCode] = scribe.cats[IO].info("Hello, World!").map { _ =>
//    ExitCode.Success
//  }

  override def run(args: List[String]): IO[ExitCode] = {
    import cats._
    val biz = new Biz[IO]
    biz.doStuff().map(_ => ExitCode.Success)
  }
}

class Biz[F[_]: MonadThrow: ScribeEffect] {
  def doStuff(): F[String] = for {
    _ <- ScribeEffect[F].info("Testing!")
  } yield {
    "done"
  }
}