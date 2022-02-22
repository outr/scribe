package scribe

object Test {
  def main(args: Array[String]): Unit = {
    val logger: Logger[Unit] = ???
    logger.info("testing")
  }
}

/**
  * Defines the current context applied when logging
  */
class LoggingContext

object LoggingContext {
  implicit def default: LoggingContext = ???
}

/**
  * Defines stored configuration for a LoggerId
  */
case class LoggerConfig()

object LoggerConfig {
  implicit def default: LoggerConfig = ???
}

class Logger[F](private val id: LoggerId) extends AnyVal {
  def log(record: LogRecord)
         (implicit context: LoggingContext,
          config: LoggerConfig): F = ???

  def log(level: Level, messages: LoggableMessage*)
         (implicit pkg: sourcecode.Pkg,
          fileName: sourcecode.FileName,
          name: sourcecode.Name,
          line: sourcecode.Line,
          context: LoggingContext,
          config: LoggerConfig): F =
    log(Logger.record(
      level = level,
      messages = messages.toList,
      pkg = pkg,
      fileName = fileName,
      name = name,
      line = line
    ))

  def trace(messages: LoggableMessage*)
           (implicit pkg: sourcecode.Pkg,
            fileName: sourcecode.FileName,
            name: sourcecode.Name,
            line: sourcecode.Line,
            context: LoggingContext,
            config: LoggerConfig): F = log(Level.Trace, messages: _*)

  def debug(messages: LoggableMessage*)
           (implicit pkg: sourcecode.Pkg,
            fileName: sourcecode.FileName,
            name: sourcecode.Name,
            line: sourcecode.Line,
            context: LoggingContext,
            config: LoggerConfig): F = log(Level.Debug, messages: _*)

  def info(messages: LoggableMessage*)
           (implicit pkg: sourcecode.Pkg,
            fileName: sourcecode.FileName,
            name: sourcecode.Name,
            line: sourcecode.Line,
            context: LoggingContext,
            config: LoggerConfig): F = log(Level.Info, messages: _*)

  def warn(messages: LoggableMessage*)
          (implicit pkg: sourcecode.Pkg,
           fileName: sourcecode.FileName,
           name: sourcecode.Name,
           line: sourcecode.Line,
           context: LoggingContext,
           config: LoggerConfig): F = log(Level.Warn, messages: _*)

  def error(messages: LoggableMessage*)
           (implicit pkg: sourcecode.Pkg,
            fileName: sourcecode.FileName,
            name: sourcecode.Name,
            line: sourcecode.Line,
            context: LoggingContext,
            config: LoggerConfig): F = log(Level.Error, messages: _*)
}

// TODO: Should this be an instantiable class to share F?
object Logger {
  val RootId: LoggerId = LoggerId(0L)

  private var id2Logger: Map[LoggerId, Logger[_]] = Map.empty
  private var name2Id: Map[String, LoggerId] = Map.empty
  private var cache = Map.empty[sourcecode.Pkg, Map[sourcecode.FileName, (String, String)]]

  private def record(level: Level,
             messages: List[LoggableMessage],
             pkg: sourcecode.Pkg,
             fileName: sourcecode.FileName,
             name: sourcecode.Name,
             line: sourcecode.Line): LogRecord = {
    val (fn, cn) = className(pkg, fileName)
    val methodName = name.value match {
      case "anonymous" | "" => None
      case v => Option(v)
    }
    val lineNumber = line.value match {
      case v if v < 0 => None
      case v => Some(v)
    }
    LogRecord(
      level = level,
      value = level.value,
      messages = messages,
      fileName = fn,
      className = cn,
      methodName = methodName,
      line = lineNumber,
      column = None,
      thread = Thread.currentThread(),
      timeStamp = System.currentTimeMillis()
    )
  }

  def className(pkg: sourcecode.Pkg, fileName: sourcecode.FileName): (String, String) = cache.get(pkg) match {
    case Some(m) => m.get(fileName) match {
      case Some(value) => value
      case None =>
        val value = generateClassName(pkg, fileName)
        Logger.synchronized {
          cache += pkg -> (m + (fileName -> value))
        }
        value
    }
    case None =>
      val value = generateClassName(pkg, fileName)
      Logger.synchronized {
        cache += pkg -> Map(fileName -> value)
      }
      value
  }

  private def generateClassName(pkg: sourcecode.Pkg, fileName: sourcecode.FileName): (String, String) = {
    val backSlash = fileName.value.lastIndexOf('\\')
    val fn = if (backSlash != -1) {
      fileName.value.substring(backSlash + 1)
    } else {
      fileName.value
    }
    fn -> s"${pkg.value}.${fn.substring(0, fn.length - 6)}"
  }
}

final case class LoggerId(value: Long)

object LoggerId {
  def apply(): LoggerId = LoggerId(scala.util.Random.nextLong())
}

case class LogRecord(level: Level,
                     value: Double,
                     messages: List[LoggableMessage],
                     fileName: String,
                     className: String,
                     methodName: Option[String],
                     line: Option[Int],
                     column: Option[Int],
                     thread: Thread,
                     timeStamp: Long)

case class Level(name: String, value: Double) {
  def namePadded: String = Level.padded(this)

  Level.add(this)
}

object Level {
  private var maxLength = 0

  private var map = Map.empty[String, Level]
  private var padded = Map.empty[Level, String]

  implicit final val LevelOrdering: Ordering[Level] = Ordering.by[Level, Double](_.value).reverse

  val Trace: Level = Level("TRACE", 100.0)
  val Debug: Level = Level("DEBUG", 200.0)
  val Info: Level = Level("INFO", 300.0)
  val Warn: Level = Level("WARN", 400.0)
  val Error: Level = Level("ERROR", 500.0)
  val Fatal: Level = Level("FATAL", 600.0)

  def add(level: Level): Unit = synchronized {
    val length = level.name.length
    map += level.name.toLowerCase -> level
    if (length > maxLength) {
      maxLength = length
      padded = map.map {
        case (_, level) => level -> level.name.padTo(maxLength, " ").mkString
      }
    } else {
      padded += level -> level.name.padTo(maxLength, " ").mkString
    }
  }

  def get(name: String): Option[Level] = map.get(name.toLowerCase)

  def apply(name: String): Level = get(name).getOrElse(throw new RuntimeException(s"Level not found by name: $name"))
}

trait LoggableMessage

object LoggableMessage {
  implicit def string2LoggableMessage(message: String): LoggableMessage = ???
}