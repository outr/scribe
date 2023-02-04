package spec

import fabric._
import fabric.parse.Json
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scribe.Logger
import scribe.json._
import scribe.json.event._
import scribe.json.fabric._
import scribe.util.Time
import scribe.writer.CacheWriter
import codec._

class FabricJsonWriterSpec extends AnyWordSpec with Matchers {
  "JsonWriter" should {
    var time: Long = 1609488000000L

    def logger: Logger = Logger("jsonWriterSpec")

    val cache = new CacheWriter

    "initialize properly" in {
      logger
        .orphan()
        .withHandler(writer = new JsonWriter[DataDogRecord](cache, Map.empty))
        .replace()
      Time.function = () => time
    }
    "log a simple message" in {
      cache.clear()
      logger.info("Hello, Json!")
      cache.output.length should be(1)
      val json = Json.parse(cache.output.head.plainText)
      json("date").asString should be("2021-01-01")
      json("line").asInt should be(32)
      json("fileName").asString should be("FabricJsonWriterSpec.scala")
      json("message") should be(Str("Hello, Json!"))
    }
    "log a simple message and exception" in {
      cache.clear()
      time += 1000L * 60 * 60 * 24
      logger.warn("Failure, Json!", new RuntimeException("Failure!"))
      cache.output.length should be(1)
      val json = Json.parse(cache.output.head.plainText)
      json("date").asString should be("2021-01-02")
      json("line").asInt should be(43)
      json("fileName").asString should be("FabricJsonWriterSpec.scala")
      json("message") should be(Str("Failure, Json!"))
    }
    "log a JSON message" in {
      cache.clear()
      time += 1000L * 60 * 60 * 24
      logger.info(obj(
        "message" -> "JSON Message!"
      ))
      cache.records.length should be(1)
      val json = cache.records.head.messages.head.value.asInstanceOf[Value]
      json("message") should be(Str("JSON Message!"))
    }
  }
}