package spec

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.TimeZone
import org.slf4j.{LoggerFactory, MDC}
import scribe.handler.LogHandler
import scribe.output.LogOutput
import scribe.output.format.{ASCIIOutputFormat, OutputFormat}
import scribe.util.Time
import scribe.format._
import scribe.format
import scribe.writer.Writer
import scribe.{Level, LogRecord, Logger}

class SLF4JSpec extends AnyWordSpec with Matchers {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

  private var logs: List[LogRecord] = Nil
  private var logOutput: List[String] = Nil
  private val writer = new Writer {
    override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = {
      logs = record :: logs
      logOutput = output.plainText :: logOutput
    }
  }
  private val recordHolder = LogHandler(
    writer = writer,
    minimumLevel = Some(Level.Info),
    formatter = formatter"$dateFull ${string("[")}$levelColoredPaddedRight${string("]")} ${green(position)} - ${format.messages}$mdc"
  )

  "SLF4J" should {
    "set the time to an arbitrary value" in {
      OutputFormat.default = ASCIIOutputFormat
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
      r.logOutput.plainText should be("Hello World!")
      r.className should be("spec.SLF4JSpec")
      logs = Nil
    }
    "verify Scribe wrote value" in {
      logOutput.size should be(1)
      val s = logOutput.head
      s should be("2018.11.16 13:49:51:920 [INFO ] spec.SLF4JSpec - Hello World!")
    }
    "use MDC" in {
      MDC.put("name", "John Doe")
      val logger = LoggerFactory.getLogger(getClass)
      logger.info("A generic name")
      logOutput.head should be("2018.11.16 13:49:51:920 [INFO ] spec.SLF4JSpec - A generic name (name: John Doe)")
    }
    "clear MDC" in {
      MDC.clear()
      val logger = LoggerFactory.getLogger(getClass)
      logger.info("MDC cleared")
      logOutput.head should be("2018.11.16 13:49:51:920 [INFO ] spec.SLF4JSpec - MDC cleared")
    }
    "make sure logging nulls doesn't error" in {
      val logger = LoggerFactory.getLogger(getClass)
      logger.error(null)
      logs.length should be(3)
      logOutput.head should be("2018.11.16 13:49:51:920 [ERROR] spec.SLF4JSpec - null")
    }
  }
}