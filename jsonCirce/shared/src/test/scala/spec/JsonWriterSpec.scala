package spec

import io.circe.{Json, JsonObject}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scribe.Logger
import scribe.util.Time
import scribe.writer.CacheWriter
import scribe.json.ScribeCirceJsonSupport._
import io.circe.parser.parse
import org.scalatest.Inside

class JsonWriterSpec extends AnyWordSpec with Matchers with Inside {
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
      inside(parse(cache.output.head.plainText).toOption.flatMap(_.asObject)) {
        case Some(json) => {
          json("line").flatMap(_.asNumber).flatMap(_.toLong) should be(Some(29))
          json("fileName").flatMap(_.asString) should be(Some("JsonWriterSpec.scala"))
          json("messages").flatMap(_.as[List[String]].toOption) should be(Some(List("Hello, Json!")))
        }
      }
    }

    "log a simple message and exception" in {
      cache.clear()
      time += 1000L * 60 * 60 * 24
      logger.warn("Failure, Json!", new RuntimeException("Failure!"))
      cache.output.length should be(1)
      inside(parse(cache.output.head.plainText).toOption.flatMap(_.asObject)) {
        case Some(json) =>
          json("@timestamp").flatMap(_.asString) should be(Some("2021-01-02T08:00:00Z"))
          json("line").flatMap(_.asNumber).flatMap(_.toLong) should be(Some(43))
          json("fileName").flatMap(_.asString) should be(Some("JsonWriterSpec.scala"))
          json("messages").flatMap(_.as[List[String]].toOption) should be(Some(List("Failure, Json!")))
      }
    }

    "log a JSON message" in {
      cache.clear()
      time += 1000L * 60 * 60 * 24
      logger.info(Json.fromFields(List("message" -> Json.fromString("JSON Message!"))))
      cache.records.length should be(1)
      val json = cache.records.head.messages.head.value.asInstanceOf[Json]
      inside(json.asObject) {
        case Some(jso) =>
          jso("message").flatMap(_.asString) should be(Some("JSON Message!"))
      }
    }
  }
}