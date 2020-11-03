package scribe

import java.io.{File, FileInputStream, InputStream}
import java.net.URL
import java.nio.file.{Files, Path, Paths}
import java.util.Properties

import scribe.handler.{LogHandler, SynchronousLogHandler}
import scribe.modify.LevelFilter
import scribe.modify.LevelFilter._

import scala.jdk.CollectionConverters._

import scala.language.implicitConversions

object Log4JMigration {
  private val RootLoggerRegex = """log4j[.](rootLogger|rootCategory)""".r
  private val AppenderClass = """log4j[.]appender[.]([a-zA-Z0-9]+)""".r
  private val AppenderLayout = """log4j[.]appender[.]([a-zA-Z0-9]+)[.]layout""".r
  private val AppenderLayoutConversionPattern = """log4j[.]appender[.]([a-zA-Z0-9]+)[.]layout[.]ConversionPattern""".r
  private val LoggerRegex = """log4j[.]logger[.](.+)""".r

  def load(): Int = {
    // Load from classloader
    val loaded = (0 :: getClass.getClassLoader.getResources("log4j.properties").asScala.toList.map(load)).sum
    // Load from disk
    val path = Paths.get("log4j.properties")
    if (Files.exists(path)) {
      load(path)
      loaded + 1
    } else {
      loaded
    }
  }

  def load(url: URL): Int = load(url.openStream())

  def load(file: File): Int = load(new FileInputStream(file))

  def load(path: Path): Int = load(Files.newInputStream(path))

  def load(input: InputStream): Int = try {
    val p = new Properties
    p.load(input)
    val m = new Log4JMigration

    try {
      p.entrySet().asScala.map(e => (e.getKey.toString, e.getValue.toString)).toList.sortBy(_._1).map {
        case (key, value) => m(key, value)
      }.count(identity)
    } finally {
      m.finish()
    }
  } finally {
    input.close()
  }
}

class Log4JMigration private() {
  import Log4JMigration._

  private var handlers = Map.empty[String, SynchronousLogHandler]

  def apply(key: String, value: String): Boolean = key match {
    case AppenderClass(name) => value match {
      case "org.apache.log4j.ConsoleAppender" => {
        handlers += name -> LogHandler()
        true
      }
      case _ => {
        scribe.warn(s"Unsupported appender: $value for $name")
        false
      }
    }
    case AppenderLayout(_) => false                                             // Ignore layouts
    case AppenderLayoutConversionPattern(_) => false                            // Ignore layouts
    case LoggerRegex(className) => {
      val filter = levelFilter(value)
      Logger(className).withModifier(filter).replace()
      true
    }
    case RootLoggerRegex(_) => {
      val list = value.split(',').map(_.trim).toList
      val filter = levelFilter(list.head)
      val handlers = list.tail.map(this.handlers.apply)
      Logger.root.withModifier(filter).replace()
      handlers.foreach { h =>
        Logger.root.withHandler(h).replace()
      }
      true
    }
    case _ => {
      scribe.warn(s"Unsupported log4j property: $key = $value")
      false
    }
  }

  private def levelFilter(value: String): LevelFilter = value match {
    case "OFF" => ExcludeAll
    case "TRACE" => >=(Level.Trace)
    case "DEBUG" => >=(Level.Debug)
    case "INFO" => >=(Level.Info)
    case "WARN" => >=(Level.Warn)
    case "ERROR" => >=(Level.Error)
    case "FATAL" => >=(Level.Fatal)
    case _ => throw new RuntimeException(s"Unsupported level name: $value")
  }

  def finish(): Unit = {}
}