package spec

import cats.MonadThrow
import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.all._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import scribe.handler.LogHandler
import scribe.{LogRecord, Logger, Scribe}

class ScribeSpec extends AsyncWordSpec with AsyncIOSpec with Matchers {
  "ScribeEffect" should {
    var messages: List[String] = Nil
    Logger.root
      .clearHandlers()
      .withHandler(new LogHandler {
        override def log(record: LogRecord): Unit = synchronized {
          messages = record.messages.map(_.logOutput.plainText) ::: messages
        }
      })
      .replace()

    "do cats.io logging" in {
      scribe.cats.io.info("1").map { _ =>
        messages should be(List("1"))
      }
    }
    "do cats[IO] logging" in {
      messages = Nil

      scribe.cats[IO].info("2").map { _ =>
        messages should be(List("2"))
      }
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
    "do reference logging" in {
      messages = Nil

      val logger = scribe.cats[IO]
      logger.info("4").map { _ =>
        messages should be(List("4"))
      }
    }
    "do existing logger logging" in {
      messages = Nil

      import scribe.cats._
      Logger.root.f[IO].info("5").map { _ =>
        messages should be(List("5"))
      }
    }
  }

  class Biz[F[_]: MonadThrow: Scribe] {
    def doStuff(): F[String] = for {
      _ <- Scribe[F].info("3")
    } yield {
      "done"
    }
  }
}