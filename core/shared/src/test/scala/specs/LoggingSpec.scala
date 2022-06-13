package specs

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.concurrent.atomic.AtomicInteger
import perfolation._
import scribe._
import scribe.data._
import scribe.filter._
import scribe.format.{FormatBlock, Formatter}
import scribe.handler.{LogHandler, SynchronousLogHandle}
import scribe.message.LoggableMessage
import scribe.modify.{LevelFilter, LogBooster}
import scribe.output.format.{HTMLOutputFormat, OutputFormat}
import scribe.output.{LogOutput, TextOutput}
import scribe.util.Time
import scribe.writer.{CacheWriter, NullWriter, Writer}

import scala.collection.mutable.ListBuffer
import scala.language.implicitConversions

class LoggingSpec extends AnyWordSpec with Matchers with Logging {
  val expectedTestFileName = "LoggingTestObject.scala"

  "Logging" should {
    val handler = new TestingHandler
    val testObject = new LoggingTestObject(handler)
    "set up the logging" in {
      handler.clear()
      logger.withHandler(handler).replace()
      Logger("specs").orphan().replace()
      loggerName should be("specs.LoggingSpec")
    }
    "properly validate include" in {
      logger.includes(Level.Info) should be(true)
      logger.includes(Level.Debug) should be(true)
      logger.includes(Level.Error) should be(true)
    }
    "confirm logging parentage" in {
      Logger(logger.parentId.get) should be(Logger("specs"))
    }
    "have no logged entries yet" in {
      handler.records.length should be(0)
    }
    "log a single entry after info log" in {
      logger.info("Info Log")
      handler.records.length should be(1)
    }
    "log a second entry after debug log" in {
      logger.debug("Debug Log")
      handler.records.length should be(2)
    }
    "ignore the third entry after reconfiguring without debug logging" in {
      logger
        .withMinimumLevel(Level.Info)
        .replace()
      handler.records.length should be(2)
      logger.debug("Debug Log 2")
      handler.records.length should be(2)
    }
    "boost the this logging instance" in {
      logger.withModifier(LogBooster.multiply(2.0, Priority.Critical)).replace()
      logger.debug("Debug Log 3")
      handler.records.length should be(3)
    }
    "not increment when logging to the root logger" in {
      Logger.root.error("Error Log 1")
      handler.records.length should be(3)
    }
    "log using no arguments" in {
      logger.info()
      handler.records.length should be(4)
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
      val line = Some(13)
      handler.clear()
      testObject.testLogger()
      handler.records.length should be(1)
      handler.records.head.methodName should be(Some("testLogger"))
      handler.records.head.className should be("specs.LoggingTestObject")
      handler.records.head.line should be(line)
      handler.records.head.fileName should be(expectedTestFileName)
      FormatBlock.Position.abbreviate(maxLength = 1, removeEntries = false)
        .format(handler.records.head)
        .plainText should be("s.LoggingTestObject.testLogger:13")
    }
    "write a log message with an anonymous function" in {
      val line = Some(9)
      handler.clear()
      testObject.testAnonymous()
      handler.records.length should be(1)
      handler.records.head.methodName should be(None)
      handler.records.head.className should be("specs.LoggingTestObject")
      handler.records.head.line should be(line)
      handler.records.head.fileName should be(expectedTestFileName)
    }
    "write an exception" in {
      val line = Some(21)
      handler.clear()
      testObject.testException()
      handler.records.length should be(1)
      handler.records.head.methodName should be(Some("testException"))
      handler.records.head.className should be("specs.LoggingTestObject")
      handler.records.head.line should be(line)
      handler.records.head.logOutput.plainText should startWith("java.lang.RuntimeException: Testing")
      handler.records.head.fileName should be(expectedTestFileName)
    }
    "utilize MDC logging" in {
      val logs = ListBuffer.empty[String]
      val logger = Logger.empty.withHandler(
        formatter = LoggingSpec.mdcFormatter,
        writer = new Writer {
          override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = logs += output.plainText
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
      logs(pos) should be("UNSET - A")
      pos += 1
      logs(pos) should be("[First], Second - B")
      pos += 1
      logs(pos) should be("Second - D")
      pos += 1
      logs(pos) should be("UNSET - E")
    }
    "utilize MDC functional logging" in {
      import scribe.format._
      val logs = ListBuffer.empty[String]
      val logger = Logger.empty.withHandler(
        formatter = Formatter.simple,
        writer = new Writer {
          override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = logs += output.plainText
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
          override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = logs += output.plainText
        }
      )

      val original = Time.function
      try {
        var time = System.currentTimeMillis()
        Time.function = () => time

        logger.elapsed {
          logger.info("A")
          time += 1000L
          logger.info("B")
          time += 500L
          logger.info("C")
        }
        logger.info("D")
      } finally {
        Time.function = original
      }

      var pos = 0
      logs(pos) should be("A (elapsed: 0.00s)")
      pos += 1
      logs(pos) should be("B (elapsed: 1.00s)")
      pos += 1
      logs(pos) should be("C (elapsed: 1.50s)")
      pos += 1
      logs(pos) should be("D")
    }
    "verify record evaluations occur exactly once" in {
      val evaluated = new AtomicInteger(0)
      def message(): String = {
        evaluated.incrementAndGet().toString
      }
      Logger("once").withHandler(new LogHandler {
        override def log(record: LogRecord): Unit = {
          // A handler must exist
        }
      }).replace()
      Logger("once").info(message())
      evaluated.get() should be(1)
      Logger("once").clearHandlers().replace()
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
            override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = logs += output.plainText
          }
        )
      logger.logDirect(Level.Warn, List("Included"), className = "org.apache.flink.api.Included")
      logs.toList should be(List("Included"))
      logger.logDirect(Level.Info, List("Excluded"), className = "org.apache.flink.api.Excluded")
      logs.toList should be(List("Included"))
      logger.logDirect(Level.Info, List("Ignored"), className = "test.Ignored")
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
            override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = logs += output.plainText
          },
          minimumLevel = Some(Level.Info)
        )
      logger.logDirect(Level.Warn, List("Included 1"), className = "org.apache.flink.api.Included")
      logs.toList should be(List("Included 1"))
      logger.logDirect(Level.Trace, List("Included 2"), className = "org.apache.flink.api.Included")
      logs.toList should be(List("Included 1", "Included 2"))
      logger.logDirect(Level.Trace, List("Excluded"), className = "org.apache.flink.Excluded")
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
            override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = logs += output.plainText
          },
          minimumLevel = Some(Level.Info)
        )
      logger.logDirect(Level.Debug, List("Included 1"), className = "org.package1.Included")
      logs.toList should be(List("Included 1"))
      logger.logDirect(Level.Debug, List("Included 2"), className = "org.package2.Included")
      logs.toList should be(List("Included 1", "Included 2"))
      logger.logDirect(Level.Info, List("Excluded 1"), className = "org.package3.Excluded")
      logs.toList should be(List("Included 1", "Included 2"))
      logger.logDirect(Level.Error, List("Included 3"), className = "org.package3.Included")
      logs.toList should be(List("Included 1", "Included 2", "Included 3"))
    }
    "validate minimum level override support" in {
      var logged = List.empty[String]

      def verify(expected: String*): Unit = try {
        logged should be(expected.toList)
      } finally {
        logged = Nil
      }

      val parent = Logger.empty.orphan().withMinimumLevel(Level.Error).withHandler(new LogHandler {
        override def log(record: LogRecord): Unit = {
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
          override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit =
            records = record.messages.map(_.logOutput.plainText).mkString(" ") :: records
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
    "validate logger has proper parentage" in {
      Logger.namesFor(logger.id) should be(Set("specs.LoggingSpec"))
      logger should be(Logger[LoggingSpec])
      val p1 = Logger(logger.parentId.get)
      Logger.namesFor(p1.id) should be(Set("specs"))
      p1.parentId should be(None)
    }
    "validate a more complicated scenario" in {
      var records = List.empty[String]
      val base = Logger
        .empty
        .orphan()
        .withMinimumLevel(Level.Info)
        .withHandler(new LogHandler {
          override def log(record: LogRecord): Unit = records = record.logOutput.plainText :: records
        })
        .replace()
      Logger("com.example1").withParent(base.id).withModifier(boosted(Level.Trace, Level.Info)).replace()
      val l2 = Logger("com.example1.Test").replace()
      Logger.namesFor(l2.id) should be(Set("com.example1.Test"))
      l2.info("One")
      records should be(List("One"))
      l2.trace("Two")
      records should be(List("Two", "One"))
    }
    "verify minimum levels convenience method" in {
      import scribe._

      Logger.minimumLevels(
        "com.e1" -> Level.Info,
        "specs" -> Level.Error,
        classOf[Int] -> Level.Trace
      )

      Logger("com.e1").modifiers.head shouldBe a[LevelFilter]
      Logger("specs").modifiers.head shouldBe a[LevelFilter]
      Logger[Int].modifiers.head shouldBe a[LevelFilter]
    }
    "validate the default padded name for Level is correct" in {
      Level.Info.namePadded should be("INFO ")
    }
    "log a special class" in {
      var logged = List.empty[User]

      case class User(name: String, age: Int)
      val logger = Logger().orphan().withHandler(new LogHandler {
        override def log(record: LogRecord): Unit = record.messages.head.value match {
          case u: User => logged = u :: logged
          case _ => // Ignore others
        }
      })

      implicit def loggableUser(user: => User): LoggableMessage =
        LoggableMessage[User](u => new TextOutput(s"{name: ${u.name}, age: ${u.age}}"))(user)

      logger.info(User("John Doe", 21))

      logged should be(List(User("John Doe", 21)))
    }
    "access non-String values in MDC" in {
      var logged = List.empty[User]

      case class User(name: String, age: Int)
      val logger = Logger().orphan().withHandler(new LogHandler {
        override def log(record: LogRecord): Unit = MDC.get("user").foreach {
          case u: User => logged = u :: logged
        }
      })

      logger("user" -> User("John Doe", 21)) {
        logger.info("Hello")
      }

      logged should be(List(User("John Doe", 21)))
    }
    "access non-String values in `data`" in {
      var logged = List.empty[User]

      case class User(name: String, age: Int)
      val logger = Logger().orphan().withHandler(new LogHandler {
        override def log(record: LogRecord): Unit = record.get("user").foreach {
          case u: User => logged = u :: logged
        }
      })

      logger.set("user", User("John Doe", 21)).info("Hello")

      logged should be(List(User("John Doe", 21)))
    }
    "filter logs with two level filters with alwaysApply" in {
      val writer = new CacheWriter
      import scribe.format._
      val logger = Logger.empty
        .orphan()
        .withMinimumLevel(Level.Info)
        .withHandler(
          formatter = formatter"${scribe.format.messages}",
          minimumLevel = Some(Level.Error),
          writer = writer
        )
      logger.info("info")
      logger.error("error")
      writer.output.map(_.plainText) should be(List("error"))
    }
    // TODO: figure out why the hour is 8 hours off on 2.11
    /*"use HTMLOutputFormat to log something" in {
      val MomentInTime = 1606235160799L
      Time.contextualize(MomentInTime) {
        val b = new StringBuilder
        val writer = new Writer {
          override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = {
            outputFormat(output, b.append(_))
          }
        }
        val logger = Logger().orphan().withHandler(writer = writer, outputFormat = HTMLOutputFormat)
        logger.info("Hello, HTML!")
        b.toString() should be("""<div class="record">2020.11.24 08:26:00:799 [<span style="color: blue">INFO </span>] <span style="color: green">specs.LoggingSpec.LoggingSpec:446</span> - <span style="color: gray">Hello, HTML!</span></div>""")
      }
    }*/
  }
}

object LoggingSpec {
  import scribe.format._
  val mdcFormatter: Formatter = formatter"${mdc("test1", prefix = string("["), postfix = string("], "))}${mdc("test2", "UNSET")} - $messages"
}