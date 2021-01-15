package scribe

import java.io.{File, FileInputStream, InputStream}
import java.net.URL
import java.nio.file.{Files, Path, Paths}
import java.util.Properties
import moduload.Moduload
import scribe.handler.{LogHandler, SynchronousLogHandler}
import scribe.modify.LevelFilter
import scribe.modify.LevelFilter._

import scala.jdk.CollectionConverters._
import scala.language.implicitConversions
import scala.util.Try

object Log4JMigration extends Moduload {
  private val RootLoggerRegex = """log4j[.](rootLogger|rootCategory)""".r
  private val AppenderClass = """log4j[.]appender[.]([a-zA-Z0-9]+)""".r
  private val AppenderLayout = """log4j[.]appender[.]([a-zA-Z0-9]+)[.]layout""".r
  private val AppenderLayoutConversionPattern = """log4j[.]appender[.]([a-zA-Z0-9]+)[.]layout[.]ConversionPattern""".r
  private val LoggerRegex = """log4j[.]logger[.](.+)""".r

  override def load(): Unit = apply()

  override def error(t: Throwable): Unit = scribe.error("Error loading Log4JMigration", t)

  def apply(): Int = {
    val path = Paths.get("log4j.properties")
    if (Files.exists(path)) {
      // Load from disk
      load(path)
    } else {
      // Load from classloader
      Try(Option(getClass.getClassLoader.getResource("log4j.properties"))).toOption.flatten match {
        case Some(url) => load(url)
        case None => 0
      }
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

  def apply(key: String, value: String): Boolean = {
    def parse(): (LevelFilter, List[LogHandler]) = {
      val list = value.split(',').map(_.trim).filter(_.nonEmpty).toList
      (levelFilter(list.head), list.tail.map(handlers.apply))
    }
    key match {
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
        val (filter, handlers) = parse()
        Logger(className).withModifier(filter).replace()
        if (handlers.nonEmpty) {
          Logger(className).clearHandlers().replace()
        }
        handlers.foreach { h =>
          Logger(className).withHandler(h).replace()
        }
        true
      }
      case RootLoggerRegex(_) => {
        val (filter, handlers) = parse()
        Logger.root.withModifier(filter).replace()
        if (handlers.nonEmpty) {
          Logger.root.clearHandlers().replace()
        }
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
  }

  private def levelFilter(value: String): LevelFilter = value.toUpperCase match {
    case "OFF" => ExcludeAll
    case "TRACE" => >=(Level.Trace)
    case "DEBUG" => >=(Level.Debug)
    case "INFO" => >=(Level.Info)
    case "WARN" => >=(Level.Warn)
    case "ERROR" => >=(Level.Error)
    case "FATAL" => >=(Level.Fatal)
    case _ => {
      scribe.error(s"Unsupported level name: $value. Continuing with OFF.")
      ExcludeAll
    }
  }

  def finish(): Unit = {}
}