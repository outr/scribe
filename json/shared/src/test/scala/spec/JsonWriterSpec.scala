package spec

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scribe.Logger
import scribe.json.JsonWriter
import scribe.util.Time
import scribe.writer.CacheWriter

class JsonWriterSpec extends AnyWordSpec with Matchers {
  "JsonWriter" should {
    def logger: Logger = Logger("jsonWriterSpec")
    val cache = new CacheWriter

    "initialize properly" in {
      logger
        .orphan()
        .withHandler(writer = new JsonWriter(cache))
        .replace()
      Time.function = () => 1609488000000L
    }
    "log a simple message" in {
      logger.info("Hello, Json!")
      cache.output.length should be(1)
      cache.output.head.plainText should startWith("""{"level":"INFO","levelValue":300,"message":"Hello, Json!","fileName":"JsonWriterSpec.scala","className":"spec.JsonWriterSpec","methodName":["JsonWriterSpec"],"line":[23],"column":[],"data":{},"throwable":[],"timeStamp":1609488000000""")
    }
  }
}