package spec

import org.scalatest.{Matchers, WordSpec}
import org.slf4j.LoggerFactory
import scribe.handler.LogHandler
import scribe.modify.LogModifier
import scribe.{Level, LogRecord, Logger, Priority}

class SLF4JSpec extends WordSpec with Matchers {
  private var logs: List[LogRecord[_]] = Nil
  private val recordHolder = LogHandler.default.withMinimumLevel(Level.Info).withModifier(new LogModifier {
    override def priority: Priority = Priority.Normal

    override def apply[M](record: LogRecord[M]): Option[LogRecord[M]] = {
      logs = record :: logs
      Some(record)
    }
  })

  "SLF4J" should {
    "remove existing handlers from Root" in {
      Logger.update(Logger.rootName)(_.clearHandlers())
    }
    "add a testing handler" in {
      Logger.update(Logger.rootName)(_.withHandler(recordHolder))
    }
    "verify not records are in the RecordHolder" in {
      logs.isEmpty should be(true)
    }
    "log to Scribe" in {
      val logger = LoggerFactory.getLogger(getClass)
      logger.info("Hello World!")
    }
    "verify Scribe received the record" in {
      logs.size should be(1)
      val r = logs.head
      r.level should be(Level.Info)
      r.message should be("Hello World!")
      logs = Nil
    }
  }
}