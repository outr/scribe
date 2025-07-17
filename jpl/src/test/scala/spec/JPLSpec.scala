package spec

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scribe.format._
import scribe.handler.LogHandler
import scribe.output.LogOutput
import scribe.output.format.{ASCIIOutputFormat, OutputFormat}
import scribe.util.Time
import scribe.writer.Writer
import scribe.{Level, LogRecord, Logger, format}

import java.util.{ListResourceBundle, TimeZone}

class JPLSpec extends AnyWordSpec with Matchers {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

  private val className = "spec.JPLSpec"
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
    formatter =
      formatter"$dateFull ${string("[")}$levelColoredPaddedRight${string("]")} ${green(position)} - ${format.messages}$mdc"
  )

  "JPL" should {
    TimeZone.setDefault(TimeZone.getTimeZone("America/Chicago"))

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
    "verify no records are in the RecordHolder" in {
      logs.isEmpty should be(true)
    }
    "get loaded by the ServiceLoader" in {
      val finder = System.LoggerFinder.getLoggerFinder
      finder.getClass.getName should be("scribe.jpl.ScribeSystemLoggerFinder")
    }
    "log to Scribe" in {
      val logger = System.getLogger(className)
      logger.log(System.Logger.Level.INFO, "Hello World!")
    }
    "verify Scribe received the record" in {
      logs.size should be(1)
      val r = logs.head
      r.level should be(Level.Info)
      r.logOutput.plainText should be("Hello World!")
      r.className should be("spec.JPLSpec")
      logs = Nil
    }
    "verify Scribe wrote value" in {
      logOutput.size should be(1)
      val s = logOutput.head
      s should be("2018.11.16 07:49:51:920 [INFO ] spec.JPLSpec - Hello World!")
    }
    "use the given ResourceBundle" in {
      val bundle = new ListResourceBundle {
        def getContents: Array[Array[AnyRef]] =
          Array(Array[AnyRef]("name", "John Doe"), Array[AnyRef]("age", 42: Integer))
      }
      val logger = System.getLogger(className)
      logger.log(System.Logger.Level.INFO, bundle, "name")
      logOutput.head should be("2018.11.16 07:49:51:920 [INFO ] spec.JPLSpec - John Doe")
      logger.log(System.Logger.Level.INFO, bundle, "age")
      logOutput.head should be("2018.11.16 07:49:51:920 [INFO ] spec.JPLSpec - 42")
    }
    "use the given MessageFormat pattern" in {
      val logger = System.getLogger(className)
      logger.log(System.Logger.Level.INFO, "name: {0} {1}, age: {2, number}", "John", "Doe", 42)
      logOutput.head should be("2018.11.16 07:49:51:920 [INFO ] spec.JPLSpec - name: John Doe, age: 42")
    }
    "use the given MessageFormat pattern in a ResourceBundle" in {
      val bundle = new ListResourceBundle {
        def getContents: Array[Array[AnyRef]] =
          Array(Array[AnyRef]("pattern", "name: {0} {1}, age: {2, number}"))
      }
      val logger = System.getLogger(className)
      logger.log(System.Logger.Level.INFO, bundle, "pattern", "John", "Doe", 42)
      logOutput.head should be("2018.11.16 07:49:51:920 [INFO ] spec.JPLSpec - name: John Doe, age: 42")
    }
    "make sure logging nulls doesn't error" in {
      val logger = System.getLogger(className)
      logger.log(System.Logger.Level.ERROR, null: String)
      logs.length should be(5)
      logOutput.head should be("2018.11.16 07:49:51:920 [ERROR] spec.JPLSpec - null")
    }
  }
}
