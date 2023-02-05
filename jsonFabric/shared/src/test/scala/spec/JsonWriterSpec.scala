package spec

import fabric._
import fabric.io.{Format, JsonParser}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scribe.Logger
import scribe.json.ScribeFabricJsonSupport._
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
        .withHandler(writer = writer(cache))
        .replace()
      Time.function = () => time
    }
    "log a simple message" in {
      cache.clear()
      logger.info("Hello, Json!")
      cache.output.length should be(1)
      val json = JsonParser(cache.output.head.plainText, Format.Json)
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
      val json = JsonParser(cache.output.head.plainText, Format.Json)
      json("date").asString should be("2021-01-02")
      json("line").asInt should be(38)
      json("fileName").asString should be("JsonWriterSpec.scala")
      json("message") should be(Str("Failure, Json!"))
    }
    "log a JSON message" in {
      cache.clear()
      time += 1000L * 60 * 60 * 24
      logger.info(obj(
        "message" -> "JSON Message!"
      ))
      cache.records.length should be(1)
      val json = cache.records.head.messages.head.value.asInstanceOf[Json]
      json("message") should be(Str("JSON Message!"))
    }
  }
}