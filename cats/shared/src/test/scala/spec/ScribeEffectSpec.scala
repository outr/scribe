package spec

import cats.MonadThrow
import cats.effect._
import cats.syntax.all._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import scribe.handler.LogHandler
import scribe.{LogRecord, Logger, ScribeEffect}

class ScribeEffectSpec extends AsyncWordSpec with AsyncIOSpec with Matchers {
  "ScribeEffect" should {
    var messages: List[String] = Nil
    Logger.root
      .clearHandlers()
      .withHandler(new LogHandler {
        override def log[M](record: LogRecord[M]): Unit = synchronized {
          messages = record.loggable(record.message.value).plainText :: messages
        }
      })
      .replace()

    "do cats.io logging" in {
      scribe.cats.io.info("1")
    }
    "verify log 1 arrived" in {
      messages should be(List("1"))
    }
    "do cats[IO] logging" in {
      messages = Nil
      scribe.cats[IO].info("2")
    }
    "verify log 2 arrived" in {
      messages should be(List("2"))
    }
    "do instantiation logging" in {
      messages = Nil

      import scribe.cats._

      val biz = new Biz[IO]
      biz.doStuff().map { s =>
        messages should be(List("3"))
        s should be("done")
      }
    }
  }

  class Biz[F[_]: MonadThrow: ScribeEffect] {
    def doStuff(): F[String] = for {
      _ <- ScribeEffect[F].info("3")
    } yield {
      "done"
    }
  }
}