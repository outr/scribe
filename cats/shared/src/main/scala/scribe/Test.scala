package scribe

import _root_.cats.effect._

object Test extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = scribe.cats[IO].info("Hello, World!").map { _ =>
    ExitCode.Success
  }
}
