package spec

import fabric.Str
import fabric.parse.Json
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scribe.Logger
import scribe.json.JsonWriter
import scribe.util.Time
import scribe.writer.CacheWriter

class JsonWriterSpec extends AnyWordSpec with Matchers {
  "JsonWriter" should {
    var time: Long = 1609488000000L
    def logger: Logger = Logger("jsonWriterSpec")
    val cache = new CacheWriter

    "initialize properly" in {
      logger
        .orphan()
        .withHandler(writer = new JsonWriter(cache))
        .replace()
      Time.function = () => time
    }
    "log a simple message" in {
      cache.clear()
      logger.info("Hello, Json!")
      cache.output.length should be(1)
      val json = Json.parse(cache.output.head.plainText)
      json("date").asString should be("2021-01-01")
      json("line").asInt should be(27)
      json("fileName").asString should be("JsonWriterSpec.scala")
      json("message") should be(Str("Hello, Json!"))
    }
    "log a simple message and exception" in {
      cache.clear()
      time += 1000L * 60 * 60 * 24
      logger.warn("Failure, Json!", new RuntimeException("Failure!"))
      cache.output.length should be(1)
      val json = Json.parse(cache.output.head.plainText)
      json("date").asString should be("2021-01-02")
      json("line").asInt should be(39)
      json("fileName").asString should be("JsonWriterSpec.scala")
      json("message") should be(Str("Failure, Json!"))
    }
  }
}