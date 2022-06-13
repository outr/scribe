package spec

import fabric.parse.Json
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
      val json = Json.parse(cache.output.head.plainText)
      json("date").asString should be("2021-01-01")
      json("line").asInt should be(24)
      json("fileName").asString should be("JsonWriterSpec.scala")
      json("messages").asVector.map(_.asString) should be(Vector("Hello, Json!"))
    }
  }
}