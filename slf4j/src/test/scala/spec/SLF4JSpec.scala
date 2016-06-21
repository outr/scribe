package spec

import com.outr.scribe.{Level, LogRecord, LogHandler, Logger}
import org.scalatest.{Matchers, WordSpec}
import org.slf4j.LoggerFactory

class SLF4JSpec extends WordSpec with Matchers {
  "SLF4J" should {
    "remove existing handlers from Root" in {
      Logger.root.clearHandlers()
    }
    "add a testing handler" in {
      Logger.root.addHandler(RecordHolder)
    }
    "verify not records are in the RecordHolder" in {
      RecordHolder.logs.isEmpty should be(true)
    }
    "log to Scribe" in {
      val logger = LoggerFactory.getLogger(getClass)
      logger.info("Hello World!")
    }
    "verify Scribe received the record" in {
      RecordHolder.logs.size should be(1)
      val r = RecordHolder.logs.head
      r.level should be(Level.Info)
      r.message should be("Hello World!")
      RecordHolder.clear()
    }
  }
}

object RecordHolder extends LogHandler {
  var logs: List[LogRecord] = Nil

  def clear(): Unit = synchronized {
    logs = Nil
  }

  override def level: Level = Level.Info

  override protected def publish(record: LogRecord): Unit = synchronized {
    logs = record :: logs
  }
}