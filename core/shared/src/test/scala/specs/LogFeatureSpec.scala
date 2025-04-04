package specs

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scribe.writer.CacheWriter

import scribe._

class LogFeatureSpec extends AnyWordSpec with Matchers {
  private lazy val writer = new CacheWriter()

  "LogFeature" should {
    "initialize" in {
      Logger.reset()
      Logger.root.clearHandlers().withHandler(
        writer = writer
      ).replace()
    }
    "log with data" in {
      scribe.info("testing", data("foo", "bar"))
    }
    "verify the data was written" in {
      writer.consume { records =>
        records.length should be(1)
        val record = records.head
        record.data("foo")() should be("bar")
      }
    }
    "log without data" in {
      scribe.info("testing")
    }
    "verify the data was not written" in {
      writer.consume { records =>
        records.length should be(1)
        val record = records.head
        record.data.get("foo") should be(None)
      }
    }
    "log a Throwable" in {
      val t: Throwable = new RuntimeException("Testing")
      scribe.info(t.getMessage, t)
      writer.consume { records =>
        records.map(_.messages.map(_.value.toString.takeWhile(_ != '('))) should be(List(List("Testing", "Trace")))
      }
    }
  }
}
