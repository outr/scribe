package scribe

import java.io.PrintStream

import scribe.format.Formatter
import scribe.handler.LogHandler
import scribe.modify.LogModifier
import scribe.writer.{ConsoleWriter, Writer}

import scala.util.Random

case class Logger(parentName: Option[String] = Some(Logger.rootName),
                  modifiers: List[LogModifier] = Nil,
                  handlers: List[LogHandler] = Nil,
                  overrideClassName: Option[String] = None,
                  id: Long = Random.nextLong()) extends LogSupport[Logger] with LoggerSupport {
  def reset(): Logger = copy(parentName = Some(Logger.rootName), Nil, Nil, None)
  def orphan(): Logger = copy(parentName = None)
  def withParent(name: String): Logger = copy(parentName = Some(name))
  def withHandler(handler: LogHandler): Logger = copy(handlers = handlers ::: List(handler))
  def withHandler(formatter: Formatter = Formatter.default,
                  writer: Writer = ConsoleWriter,
                  minimumLevel: Option[Level] = None,
                  modifiers: List[LogModifier] = Nil): Logger = {
    withHandler(LogHandler(formatter, writer, minimumLevel, modifiers))
  }
  def withoutHandler(handler: LogHandler): Logger = copy(handlers = handlers.filterNot(_ == handler))
  def clearHandlers(): Logger = copy(handlers = Nil)
  def withClassNameOverride(className: String): Logger = copy(overrideClassName = Option(className))
  override def setModifiers(modifiers: List[LogModifier]): Logger = copy(modifiers = modifiers)

  override def log[M](record: LogRecord[M]): Unit = {
    modifiers.foldLeft(Option(record))((r, lm) => r.flatMap(lm.apply)).foreach { r =>
      handlers.foreach(_.log(r))
      parentName.map(Logger.byName).foreach(_.log(record))
    }
  }

  def replace(): Logger = Logger.replaceById(this)
}

object Logger {
  // Keep a reference to the print streams just in case we need to redirect later
  private val systemOut = System.out
  private val systemErr = System.err

  object system {
    def out: PrintStream = systemOut
    def err: PrintStream = systemErr
  }

  val rootName: String = "root"

  def empty: Logger = Logger()
  def root: Logger = byName(rootName)

  private var map = Map.empty[String, Logger]

  def loggers: Map[String, Logger] = map

  def namesFor(logger: Logger): List[String] = map.collect {
    case (n, l) if logger eq l => n
  }.toList

  // Configure the root logger to filter anything under Info and write to the console
  root.orphan().withMinimumLevel(Level.Info).withHandler(LogHandler(writer = ConsoleWriter)).replace()

  def byName(name: String): Logger = synchronized {
    val n = fixName(name)
    map.get(n) match {
      case Some(logger) => logger
      case None => {
        val logger = Logger(Some(rootName), Nil, Nil)
        map += n -> logger
        logger
      }
    }
  }

  def replaceById(logger: Logger): Logger = synchronized {
    map = loggers.map {
      case (key, value) if logger.id == value.id => key -> logger
      case tuple => tuple
    }
    logger
  }

  def update(name: String, logger: Logger): Logger = synchronized {
    map += fixName(name) -> logger
    logger
  }

  def update(name: String)(modifier: Logger => Logger): Logger = {
    update(name, modifier(byName(name)))
  }

  private def fixName(name: String): String = name.replaceAll("[$]", "")
}