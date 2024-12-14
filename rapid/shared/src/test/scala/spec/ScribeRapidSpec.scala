package spec

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rapid.Task
import scribe.handler.LogHandler
import scribe.rapid._
import scribe.{LogRecord, Logger}
import scribe.{rapid => logger}

class ScribeRapidSpec extends AnyWordSpec with Matchers {
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

    "do rapid logging" in {
      logger.info("1").map { _ =>
        messages should be(List("1"))
      }.sync()
    }
    "do instantiation logging" in {
      messages = Nil

      val biz = new Biz
      biz.doStuff().map { s =>
        messages should be(List("3"))
        s should be("done")
      }.sync()
    }
    "do reference logging" in {
      messages = Nil

      logger.info("4").map { _ =>
        messages should be(List("4"))
      }.sync()
    }
    "do existing logger logging" in {
      messages = Nil
      Logger.root.rapid.info("5").map { _ =>
        messages should be(List("5"))
      }.sync()
    }
  }

  class Biz extends RapidLoggerSupport {
    def doStuff(): Task[String] = for {
      _ <- info("3")
    } yield {
      "done"
    }
  }
}