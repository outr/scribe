package scribe

import scribe.data.MDC
import scribe.format.Formatter
import scribe.handler.{LogHandle, LogHandler, SynchronousLogHandle}
import scribe.jul.JULHandler
import scribe.message.LoggableMessage
import scribe.modify.{LevelFilter, LogBooster, LogModifier}
import scribe.output.format.OutputFormat
import scribe.util.Time
import scribe.writer.{ConsoleWriter, Writer}
import sourcecode.{FileName, Line, Name, Pkg}

import java.io.PrintStream
import scala.reflect._
import scala.util.Try

case class Logger(parentId: Option[LoggerId] = Some(Logger.RootId),
                  modifiers: List[LogModifier] = Nil,
                  handlers: List[LogHandler] = Nil,
                  overrideClassName: Option[String] = None,
                  data: Map[String, () => Any] = Map.empty,
                  id: LoggerId = LoggerId()) extends LoggerSupport[Unit] {
  private var lastUpdate = Logger.lastChange
  private var includeStatus = Map.empty[Level, Boolean]

  lazy val isEmpty: Boolean = modifiers.isEmpty && handlers.isEmpty

  def reset(): Logger = copy(parentId = Some(Logger.RootId), Nil, Nil, None)

  def orphan(): Logger = copy(parentId = None)

  def withParent(name: String): Logger = copy(parentId = Some(Logger(name).id))

  def withParent(logger: Logger): Logger = copy(parentId = Some(logger.id))

  def withParent(id: LoggerId): Logger = copy(parentId = Some(id))

  def withHandler(handler: LogHandler): Logger = copy(handlers = handlers ::: List(handler))

  def withHandler(formatter: Formatter = Formatter.default,
                  writer: Writer = ConsoleWriter,
                  minimumLevel: Option[Level] = None,
                  modifiers: List[LogModifier] = Nil,
                  outputFormat: OutputFormat = OutputFormat.default,
                  handle: LogHandle = SynchronousLogHandle): Logger = {
    withHandler(LogHandler(formatter, writer, minimumLevel, modifiers, outputFormat, handle))
  }

  def withoutHandler(handler: LogHandler): Logger = copy(handlers = handlers.filterNot(_ == handler))

  def clearHandlers(): Logger = copy(handlers = Nil)

  def withClassNameOverride(className: String): Logger = copy(overrideClassName = Option(className))

  def setModifiers(modifiers: List[LogModifier]): Logger = copy(modifiers = modifiers.sorted)

  def clearModifiers(): Logger = setModifiers(Nil)

  def set(key: String, value: => Any): Logger = copy(data = this.data + (key -> (() => value)))

  def get(key: String): Option[Any] = data.get(key).map(_ ())

  final def withModifier(modifier: LogModifier): Logger = setModifiers(modifiers.filterNot(m => m.id.nonEmpty && m.id == modifier.id) ::: List(modifier))

  final def withoutModifier(modifier: LogModifier): Logger = setModifiers(modifiers.filterNot(m => m.id.nonEmpty && m.id == modifier.id))

  override def log(level: Level, mdc: MDC, features: LogFeature*)
                  (implicit pkg: Pkg, fileName: FileName, name: Name, line: Line): Unit = {
    if (includes(level)) {
      super.log(level, mdc, features: _*)
    }
  }

  def includes(level: Level): Boolean = {
    if (lastUpdate != Logger.lastChange) {
      includeStatus = Map.empty
      lastUpdate = Logger.lastChange
    }
    includeStatus.get(level) match {
      case Some(b) =>
        b
      case None =>
        val b = shouldLog(LogRecord.simple("", "", "", level = level))
        synchronized {
          includeStatus += level -> b
        }
        b
    }
  }

  def modifierById[M <: LogModifier](id: String, recursive: Boolean): Option[M] = {
    modifiers.find(m => m.id.nonEmpty && m.id == id).orElse {
      parentId match {
        case _ if !recursive => None
        case None => None
        case Some(pId) => Logger(pId).modifierById(id, recursive)
      }
    }.map(_.asInstanceOf[M])
  }

  def withMinimumLevel(level: Level): Logger = withModifier(LevelFilter >= level)

  def withBoost(booster: Double => Double, priority: Priority = Priority.Normal): Logger = {
    withModifier(new LogBooster(booster, priority))
  }

  def withBoostOneLevel(): Logger = withBoost(_ + 100.0)

  def withBoosted(minimumLevel: Level, destinationLevel: Level): Logger = {
    withBoost(d => if (d >= minimumLevel.value && d <= destinationLevel.value) {
      destinationLevel.value
    } else {
      d
    })
  }

  override final def log(record: LogRecord): Unit = {
    val r = if (data.nonEmpty) {
      record.copy(data = data ++ record.data)
    } else {
      record
    }
    r.modify(modifiers).foreach { r =>
      handlers.foreach(_.log(r))
      parentId.map(Logger.apply).foreach(_.log(r))
    }
  }

  protected def shouldLog(record: LogRecord): Boolean = record.modify(modifiers) match {
    case Some(_) if handlers.nonEmpty => true
    case Some(r) => parentId.map(Logger.apply).exists(p => p.shouldLog(r))
    case None => false
  }

  def replace(name: Option[String] = None): Logger = name match {
    case Some(n) => Logger.replaceByName(n, this)
    case None => Logger.replace(this)
  }

  def remove(): Unit = Logger.remove(this)

  def logDirect(level: Level,
                messages: List[LoggableMessage] = Nil,
                fileName: String = "",
                className: String = "",
                methodName: Option[String] = None,
                line: Option[Int] = None,
                column: Option[Int] = None,
                thread: Thread = Thread.currentThread(),
                timeStamp: Long = Time()): Unit = {
    log(LogRecord(
      level = level,
      levelValue = level.value,
      messages = messages,
      fileName = fileName,
      className = overrideClassName.getOrElse(className),
      methodName = methodName,
      line = line,
      column = column,
      thread = thread,
      timeStamp = timeStamp
    ))
  }
}

object Logger {
  // Keep a reference to the print streams just in case we need to redirect later
  private val systemOut = System.out
  private val systemErr = System.err

  lazy val DefaultRootMinimumLevel: Level = Option(System.getenv("SCRIBE_MINIMUM_LEVEL")).flatMap(Level.get).getOrElse(Level.Info)

  /**
   * Functionality for system output stream management
   */
  object system {
    /**
     * The standard system out (set upon initialization to represent the original, non-redirected, System.out)
     */
    def out: PrintStream = systemOut

    /**
     * The standard system err (set upon initialization to represent the original, non-redirected, System.err)
     */
    def err: PrintStream = systemErr

    /**
     * Redirects system output to Scribe's logging
     *
     * @param outLevel if set, defines the level to log System.out to (defaults to Some(Level.Info))
     * @param errLevel if set, defines the level to log System.err to (defaults to Some(Level.Error))
     * @param loggerId the loggerId to determine what logger to use when logging (defaults to Logger.RootId)
     */
    def redirect(outLevel: Option[Level] = Some(Level.Info),
                 errLevel: Option[Level] = Some(Level.Error),
                 loggerId: LoggerId = RootId): Unit = {
      if (System.out.toString != "Scribe Printer") {
        outLevel.foreach { level =>
          val os = new LoggingOutputStream(loggerId, level, className = "System", methodName = Some("out"))
          val ps = new PrintStream(os) {
            override def toString: String = "Scribe Printer"
          }
          System.setOut(ps)
        }
      } else {
        scribe.warn("System.out is already redirected")
      }
      if (System.err.toString != "Scribe Printer") {
        errLevel.foreach { level =>
          val os = new LoggingOutputStream(loggerId, level, className = "System", methodName = Some("err"))
          val ps = new PrintStream(os) {
            override def toString: String = "Scribe Printer"
          }
          System.setErr(ps)
        }
      } else {
        scribe.warn("System.err is already redirected")
      }
    }

    /**
     * Resets the System.out and System.err to the original state
     *
     * @param out if true, resets System.out (defaults to true)
     * @param err if true, resets System.err (defaults to true)
     */
    def reset(out: Boolean = true, err: Boolean = true): Unit = {
      if (out) {
        System.setOut(systemOut)
      }
      if (err) {
        System.setErr(systemErr)
      }
    }

    def installJUL(): Unit = Try(java.util.logging.LogManager.getLogManager.getLogger("").addHandler(JULHandler))
      .failed.foreach { t =>
      scribe.warn(s"Failed to install java.util.logging integration: ${t.getMessage}")
    }
  }

  val RootId: LoggerId = LoggerId(0L)

  private var lastChange: Long = 0L
  private var id2Logger: Map[LoggerId, Logger] = Map.empty
  private var name2Id: Map[String, LoggerId] = Map.empty

  resetRoot()

  // Initialize Platform-specific functionality
  Platform.init()

  def empty: Logger = Logger()

  def root: Logger = apply(RootId)

  def loggersByName: Map[String, Logger] = name2Id.map {
    case (name, id) => name -> id2Logger(id)
  }

  /**
   * Resets the global state of Scribe
   */
  def reset(): Unit = {
    id2Logger = Map.empty
    name2Id = Map.empty
    resetRoot()
  }

  def apply(name: String): Logger = get(name) match {
    case Some(logger) => logger
    case None => synchronized {
      val n = fixName(name)
      val dotIndex = n.lastIndexOf('.')
      val parentId = if (dotIndex > 0) {
        val parentName = n.substring(0, dotIndex)
        val parent = apply(parentName)
        parent.id
      } else {
        RootId
      }
      val logger = Logger(parentId = Some(parentId))
      id2Logger += logger.id -> logger
      name2Id += n -> logger.id
      lastChange = System.currentTimeMillis()
      logger
    }
  }

  def apply(id: LoggerId): Logger = get(id) match {
    case Some(logger) => logger
    case None => synchronized {
      val logger = new Logger(id = id)
      id2Logger += logger.id -> logger
      lastChange = System.currentTimeMillis()
      logger
    }
  }

  def minimumLevels(minimums: MinimumLevel*): Unit = minimums.foreach { m =>
    m.logger.withMinimumLevel(m.minimumLevel).replace()
  }

  def apply[T](implicit t: ClassTag[T]): Logger = apply(t.runtimeClass.getName)

  def get(name: String): Option[Logger] = name2Id.get(fixName(name)).flatMap(id => id2Logger.get(id))

  def get(id: LoggerId): Option[Logger] = id2Logger.get(id)

  def get[T](implicit t: ClassTag[T]): Option[Logger] = get(t.runtimeClass.getName)

  /**
    * Replaces this logger and all references to it in the global state
    */
  def replace(logger: Logger): Logger = synchronized {
    id2Logger += logger.id -> logger
    lastChange = System.currentTimeMillis()
    logger
  }

  def replaceByName(name: String, logger: Logger): Logger = synchronized {
    replace(logger)
    name2Id += fixName(name) -> logger.id
    logger
  }

  /**
    * Removes this logger from the global state and all references to it.
    */
  def remove(logger: Logger): Unit = synchronized {
    id2Logger -= logger.id
    lastChange = System.currentTimeMillis()
    val names = name2Id.collect {
      case (name, id) if logger.id == id => name
    }
    name2Id --= names
  }

  def namesFor(loggerId: LoggerId): Set[String] = name2Id.collect {
    case (name, id) if id == loggerId => name
  }.toSet

  def resetRoot(): Unit = {
    // Configure the root logger to filter anything under SCRIBE_MINIMUM_LEVEL (or INFO if not specified) and write to the console
    root
      .orphan()
      .clearModifiers()
      .withMinimumLevel(DefaultRootMinimumLevel)
      .clearHandlers()
      .withHandler()
      .replace(Some("root"))
  }

  private def fixName(name: String): String = name.replace("$", "")
}