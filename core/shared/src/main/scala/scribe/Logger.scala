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

  override def includes(level: Level): Boolean = {
    super.includes(level) &&
      (handlers.exists(_.includes(level)) || parentName.map(Logger.apply).forall(_.includes(level)))
  }

  override def log[M](record: LogRecord[M]): Unit = {
    modifiers.foldLeft(Option(record))((r, lm) => r.flatMap(lm.apply)).foreach { r =>
      handlers.foreach(_.log(r))
      parentName.map(Logger.apply).foreach(_.log(record))
    }
  }

  def replace(name: Option[String] = None): Logger = name match {
    case Some(n) => Logger.replaceByName(n, this)
    case None => Logger.replace(this)
  }
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
  def root: Logger = apply(rootName)

  private var id2Logger: Map[Long, Logger] = Map.empty
  private var name2Id: Map[String, Long] = Map.empty

  def loggersByName: Map[String, Logger] = name2Id.map {
    case (name, id) => name -> id2Logger(id)
  }

//  def namesFor(logger: Logger): List[String] = map.collect {
//    case (n, l) if logger eq l => n
//  }.toList

  // Configure the root logger to filter anything under Info and write to the console
  root.orphan().withMinimumLevel(Level.Info).withHandler(LogHandler(writer = ConsoleWriter)).replace()

  def apply(name: String): Logger = get(name) match {
    case Some(logger) => logger
    case None => synchronized {
      val n = fixName(name)
      val logger = Logger()
      id2Logger += logger.id -> logger
      name2Id += n -> logger.id
      logger
    }
  }

  def get(name: String): Option[Logger] = name2Id.get(fixName(name)).flatMap(id2Logger.get)

  def replace(logger: Logger): Logger = synchronized {
    id2Logger += logger.id -> logger
    logger
  }

  def replaceByName(name: String, logger: Logger): Logger = synchronized {
    replace(logger)
    name2Id += fixName(name) -> logger.id
    logger
  }

  private def fixName(name: String): String = name.replaceAll("[$]", "")
}