package scribe.benchmark.tester

import cats.effect._
import cats.effect.unsafe.implicits.global
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.{Logger, SelfAwareStructuredLogger}

class Log4CatsLoggingTester extends LoggingTester {
  implicit def unsafeLogger[F[_] : Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromName[F]("log4cats")

  override def run(messages: Iterator[String]): Unit = {
    val logger = Logger[IO]
    fs2.Stream
      .fromIterator[IO](messages, 1000)
      .evalTapChunk(msg => logger.info(msg))
      .compile
      .drain
      .unsafeRunSync()
  }
}