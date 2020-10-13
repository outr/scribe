package specs

import java.util.concurrent.atomic.AtomicInteger

import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import perfolation._
import scribe._
import scribe.filter._
import scribe.format.Formatter
import scribe.handler.LogHandler
import scribe.modify.LogBooster
import scribe.output.LogOutput
import scribe.writer.{NullWriter, Writer}

import scala.collection.mutable.ListBuffer

class LoggingSpec extends AnyWordSpec with Matchers with Logging {
  val expectedTestFileName = "LoggingTestObject.scala"

  "Logging" should {
    val testingModifier = new TestingModifier
    val testObject = new LoggingTestObject(testingModifier)
    val handler = new LogHandler {
      override def log[M](record: LogRecord[M]): Unit = testingModifier(record)
    }

    "set up the logging" in {
      testingModifier.clear()
      logger.withHandler(handler).replace()
      Logger("spec").orphan().replace()
      loggerName should be("specs.LoggingSpec")
    }
    "confirm logging parentage" in {
      Logger(logger.parentId.get) should be(Logger("specs"))
    }
    "have no logged entries yet" in {
      testingModifier.records.length should be(0)
    }
    "log a single entry after info log" in {
      logger.info("Info Log")
      testingModifier.records.length should be(1)
    }
    "log a second entry after debug log" in {
      logger.debug("Debug Log")
      testingModifier.records.length should be(2)
    }
    "ignore the third entry after reconfiguring without debug logging" in {
      logger
        .withoutHandler(handler)
        .withHandler(writer = NullWriter)
        .withMinimumLevel(Level.Info)
        .withModifier(testingModifier)
        .replace()
      testingModifier.records.length should be(2)
      logger.debug("Debug Log 2")
      testingModifier.records.length should be(2)
    }
    "boost the this logging instance" in {
      logger.withModifier(LogBooster.multiply(2.0, Priority.Critical)).replace()
      logger.debug("Debug Log 3")
      testingModifier.records.length should be(3)
    }
    "not increment when logging to the root logger" in {
      Logger.root.error("Error Log 1")
      testingModifier.records.length should be(3)
    }
    "log using no arguments" in {
      logger.info()
      testingModifier.records.length should be(4)
    }
    "log using 's' interpolation" in {
      val message = "Wahoo!"
      logger.info(s"It works! $message")
    }
    "log using 'f' interpolation" in {
      val d = 12.3456
      logger.info(f"It works! $d%.0f")
    }
    "log using perfolation formatting of Double" in {
      val d = 12.3456
      logger.info(s"It works! ${d.f()}")
    }
    "write a detailed log message" in {
      val line = Some(14)
      testingModifier.clear()
      testObject.testLogger()
      testingModifier.records.length should be(1)
      testingModifier.records.head.methodName should be(Some("testLogger"))
      testingModifier.records.head.className should be("specs.LoggingTestObject")
      testingModifier.records.head.line should be(line)
      testingModifier.records.head.fileName should be(expectedTestFileName)
    }
    "write a log message with an anonymous function" in {
      val line = Some(10)
      testingModifier.clear()
      testObject.testAnonymous()
      testingModifier.records.length should be(1)
      testingModifier.records.head.methodName should be(None)
      testingModifier.records.head.className should be("specs.LoggingTestObject.anonymous")
      testingModifier.records.head.line should be(line)
      testingModifier.records.head.fileName should be(expectedTestFileName)
    }
    "write an exception" in {
      val line = Some(22)
      testingModifier.clear()
      testObject.testException()
      testingModifier.records.length should be(1)
      testingModifier.records.head.methodName should be(Some("testException"))
      testingModifier.records.head.className should be("specs.LoggingTestObject")
      testingModifier.records.head.line should be(line)
      testingModifier.records.head.logOutput.plainText should startWith("java.lang.RuntimeException: Testing")
      testingModifier.records.head.fileName should be(expectedTestFileName)
    }
    "utilize MDC logging" in {
      val logs = ListBuffer.empty[String]
      val logger = Logger.empty.withHandler(
        formatter = LoggingSpec.mdcFormatter,
        writer = new Writer {
          override def write[M](record: LogRecord[M], output: LogOutput): Unit = logs += output.plainText
        }
      )

      logger.info("A")
      MDC("test1") = "First"
      MDC("test2") = "Second"
      logger.info("B")
      MDC.remove("test1")
      logger.info("D")
      MDC.clear()
      logger.info("E")

      var pos = 0
      logs(pos) should be("null, null - A")
      pos += 1
      logs(pos) should be("First, Second - B")
      pos += 1
      logs(pos) should be("null, Second - D")
      pos += 1
      logs(pos) should be("null, null - E")
    }
    "utilize MDC functional logging" in {
      import scribe.format._
      val logs = ListBuffer.empty[String]
      val logger = Logger.empty.withHandler(
        formatter = Formatter.simple,
        writer = new Writer {
          override def write[M](record: LogRecord[M], output: LogOutput): Unit = logs += output.plainText
        }
      )

      logger.info("A")
      var name = "Name 1"
      MDC("name") = name
      logger.info("B")
      name = "Name 2"
      logger.info("C")
      MDC.remove("name")
      logger.info("D")

      var pos = 0
      logs(pos) should be("A")
      pos += 1
      logs(pos) should be("B (name: Name 1)")
      pos += 1
      logs(pos) should be("C (name: Name 2)")
      pos += 1
      logs(pos) should be("D")
    }
    "utilize MDC elapsed" in {
      import scribe.format._
      val logs = ListBuffer.empty[String]
      val logger = Logger.empty.withHandler(
        formatter = Formatter.simple,
        writer = new Writer {
          override def write[M](record: LogRecord[M], output: LogOutput): Unit = logs += output.plainText
        }
      )

      var elapsed = 0L
      MDC.elapsed("timer", () => elapsed)

      logger.info("A")
      elapsed += 1000L
      logger.info("B")
      elapsed += 500L
      logger.info("C")
      MDC.remove("timer")
      logger.info("D")

      var pos = 0
      logs(pos) should be("A (timer: 0.00 seconds elapsed)")
      pos += 1
      logs(pos) should be("B (timer: 1.00 seconds elapsed)")
      pos += 1
      logs(pos) should be("C (timer: 1.50 seconds elapsed)")
      pos += 1
      logs(pos) should be("D")
    }
    "verify record evaluations occur exactly once" in {
      val evaluated = new AtomicInteger(0)
      def message(): String = {
        evaluated.incrementAndGet().toString
      }
      scribe.info(message())
      evaluated.get() should be(1)
    }
    "verify record evaluation doesn't occur at all for filtered out" in {
      val evaluated = new AtomicInteger(0)
      def message(): String = {
        new RuntimeException("Should not ever be evaluated!").printStackTrace()
        evaluated.incrementAndGet().toString
      }
      val logger = Logger().orphan().withHandler(minimumLevel = Some(Level.Info))
      logger.debug(message())
      evaluated.get() should be(0)
    }
    "filter via DSL" in {
      val logs = ListBuffer.empty[String]
      val logger = Logger
        .empty
        .orphan()
        .withModifier(
          select(packageName.startsWith("org.apache.flink.api"))
            .exclude(level < Level.Warn)
            .priority(Priority.High)
        )
        .withHandler(
          formatter = Formatter.simple,
          writer = new Writer {
            override def write[M](record: LogRecord[M], output: LogOutput): Unit = logs += output.plainText
          }
        )
      logger.logDirect(logger, Level.Warn, "Included", className = "org.apache.flink.api.Included")
      logs.toList should be(List("Included"))
      logger.logDirect(logger, Level.Info, "Excluded", className = "org.apache.flink.api.Excluded")
      logs.toList should be(List("Included"))
      logger.logDirect(logger, Level.Info, "Ignored", className = "test.Ignored")
      logs.toList should be(List("Included", "Ignored"))
    }
    "boost via DSL" in {
      val logs = ListBuffer.empty[String]
      val logger = Logger
        .empty
        .orphan()
        .withModifier(
          select(packageName.startsWith("org.apache.flink.api"))
            .boosted(Level.Trace, Level.Info)
        )
        .withHandler(
          formatter = Formatter.simple,
          writer = new Writer {
            override def write[M](record: LogRecord[M], output: LogOutput): Unit = logs += output.plainText
          },
          minimumLevel = Some(Level.Info)
        )
      logger.logDirect(logger, Level.Warn, "Included 1", className = "org.apache.flink.api.Included")
      logs.toList should be(List("Included 1"))
      logger.logDirect(logger, Level.Trace, "Included 2", className = "org.apache.flink.api.Included")
      logs.toList should be(List("Included 1", "Included 2"))
      logger.logDirect(logger, Level.Trace, "Excluded", className = "org.apache.flink.Excluded")
      logs.toList should be(List("Included 1", "Included 2"))
    }
    "multiple filters via DSL" in {
      val logs = ListBuffer.empty[String]
      val logger = Logger
        .empty
        .orphan()
        .withModifier(
          select(
            packageName.startsWith("org.package1"),
            packageName.startsWith("org.package2")
          ).boosted(Level.Trace, Level.Info)
        )
        .withModifier(select(className.startsWith("org.package3")).include(level >= Level.Error))
        .withHandler(
          formatter = Formatter.simple,
          writer = new Writer {
            override def write[M](record: LogRecord[M], output: LogOutput): Unit = logs += output.plainText
          },
          minimumLevel = Some(Level.Info)
        )
      logger.logDirect(logger, Level.Debug, "Included 1", className = "org.package1.Included")
      logs.toList should be(List("Included 1"))
      logger.logDirect(logger, Level.Debug, "Included 2", className = "org.package2.Included")
      logs.toList should be(List("Included 1", "Included 2"))
      logger.logDirect(logger, Level.Info, "Excluded 1", className = "org.package3.Excluded")
      logs.toList should be(List("Included 1", "Included 2"))
      logger.logDirect(logger, Level.Error, "Included 3", className = "org.package3.Included")
      logs.toList should be(List("Included 1", "Included 2", "Included 3"))
    }
    "validate minimum level override support" in {
      var logged = List.empty[String]

      def verify(expected: String*): Assertion = try {
        logged should be(expected.toList)
      } finally {
        logged = Nil
      }

      val parent = Logger().orphan().withMinimumLevel(Level.Error).withHandler(new LogHandler {
        override def log[M](record: LogRecord[M]): Unit = {
          logged = record.logOutput.plainText :: logged
        }
      }).replace()

      val child = Logger().withParent(parent).withMinimumLevel(Level.Info).replace()

      verify()
      parent.info("1")
      parent.error("2")
      verify("2")
      child.info("3")
      child.error("4")
      verify("4", "3")
    }
    "validate log handler modifiers DSL" in {
      var records = List.empty[String]
      val h = LogHandler(
        minimumLevel = Some(Level.Info),
        writer = new Writer {
          override def write[M](record: LogRecord[M], output: LogOutput): Unit = records = record.message.value.toString :: records
        },
        modifiers = List(
          select(
            packageName.startsWith("no.officenet"),
            packageName.startsWith("com.visena")
          )
          .boosted(Level.Trace, Level.Info)
          .priority(Priority.Important)
        )
      )
      val l = Logger().orphan().withHandler(h)
      l.log(LogRecord.simple("one", "test.scala", "no.officenet.example.One", level = Level.Trace))
      records should be(List("one"))
    }
  }
}

object LoggingSpec {
  import scribe.format._
  val mdcFormatter: Formatter = formatter"${mdc("test1")}, ${mdc("test2")} - $message"
}