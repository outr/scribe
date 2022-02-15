package scribe.benchmark.tester

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import scribe.Logger
import scribe.cats._
import scribe.file._
import scribe.format._

class ScribeEffectParallelLoggingTester extends LoggingTester {
  private lazy val fileWriter = FileWriter("logs" / "scribe-effect-par.log")
  private lazy val formatter = formatter"$date $levelPaddedRight [$threadName] $messages"
  private lazy val logger = Logger.empty.orphan().withHandler(formatter = formatter, writer = fileWriter).f[IO]

  override def init(): Unit = logger

  override def run(messages: Iterator[String]): Unit = fs2.Stream
    .fromIterator[IO](messages, 1000)
    .parEvalMap(1000)(msg => logger.info(msg))
    .compile
    .drain
    .unsafeRunSync()

  override def dispose(): Unit = fileWriter.dispose()
}