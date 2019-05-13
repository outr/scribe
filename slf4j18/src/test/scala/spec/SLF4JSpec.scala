package spec

import java.util.TimeZone

import org.scalatest.{Matchers, WordSpec}
import org.slf4j.{LoggerFactory, MDC}
import scribe.handler.LogHandler
import scribe.output.LogOutput
import scribe.util.Time
import scribe.writer.Writer
import scribe.{Level, LogRecord, Logger}

class SLF4JSpec extends WordSpec with Matchers {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

  private var logs: List[LogRecord[_]] = Nil
  private var logOutput: List[String] = Nil
  private val recordHolder = LogHandler.default.withMinimumLevel(Level.Info).withWriter(new Writer {
    override def write[M](record: LogRecord[M], output: LogOutput): Unit = {
      logs = record :: logs
      logOutput = output.plainText :: logOutput
    }
  })

  "SLF4J" should {
    "set the time to an arbitrary value" in {
      Time.function = () => 1542376191920L
    }
    "remove existing handlers from Root" in {
      Logger.root.clearHandlers().replace()
    }
    "add a testing handler" in {
      Logger.root.withHandler(recordHolder).replace()
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
      r.message.plainText should be("Hello World!")
      r.className should be("spec.SLF4JSpec")
      logs = Nil
    }
    "verify Scribe wrote value" in {
      logOutput.size should be(1)
      val s = logOutput.head
      s should be("2018.11.16 13:49:51 INFO spec.SLF4JSpec - Hello World!")
    }
    "use MDC" in {
      MDC.put("name", "John Doe")
      val logger = LoggerFactory.getLogger(getClass)
      logger.info("A generic name")
      logOutput.head should be("2018.11.16 13:49:51 INFO spec.SLF4JSpec - A generic name (name: John Doe)")
    }
    "clear MDC" in {
      MDC.clear()
      val logger = LoggerFactory.getLogger(getClass)
      logger.info("MDC cleared")
      logOutput.head should be("2018.11.16 13:49:51 INFO spec.SLF4JSpec - MDC cleared")
    }
    "make sure logging nulls doesn't error" in {
      val logger = LoggerFactory.getLogger(getClass)
      logger.error(null)
      logs.length should be(3)
      logOutput.head should be("2018.11.16 13:49:51 ERROR spec.SLF4JSpec - null")
    }
  }
}