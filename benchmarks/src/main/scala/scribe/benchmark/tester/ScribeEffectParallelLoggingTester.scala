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
  private lazy val formatter = formatter"$date $levelPaddedRight [$threadName] $message"
  private lazy val logger = Logger.empty.orphan().withHandler(formatter = formatter, writer = fileWriter).f[IO]

  override def init(): Unit = logger

  override def run(messages: Iterator[String]): Unit = {
    val io = messages.toList.map(logger.info(_)).parSequence.map(_ => ())
    io.unsafeRunSync()
  }

  override def dispose(): Unit = fileWriter.dispose()
}