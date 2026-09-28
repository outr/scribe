package specs

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scribe.Logger
import scribe.jul.JULHandler
import scribe.writer.CacheWriter

import java.util.logging.{Level, LogRecord}

class JULHandlerSpec extends AnyWordSpec with Matchers {
  private val writer = new CacheWriter

  "JULHandler" should {
    "send a record without a logger name to the root logger" in {
      Logger.root.orphan().clearHandlers().withHandler(writer = writer).replace()
      try {
        JULHandler.publish(new LogRecord(Level.WARNING, "no name"))
        writer.consume(_.map(_.messages.head.logOutput.plainText)) should be(List("no name"))
      } finally Logger.resetRoot()
    }
    "fill the message's placeholders and keep the exception" in {
      Logger("jul-spec").orphan().withHandler(writer = writer).replace()
      val record = new LogRecord(Level.SEVERE, "failed {0} of {1}")
      record.setLoggerName("jul-spec")
      record.setParameters(Array[AnyRef]("3", Integer.valueOf(5)))
      record.setThrown(new IllegalStateException("broken"))
      JULHandler.publish(record)
      writer.consume { records =>
        records.map(_.messages.head.logOutput.plainText) should be(List("failed 3 of 5"))
        records.head.messages(1).logOutput.plainText should include("IllegalStateException: broken")
      }
    }
  }
}
