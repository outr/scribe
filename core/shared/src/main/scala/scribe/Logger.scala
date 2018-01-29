package scribe

import java.io.PrintStream

import scribe.modify.{LevelFilter, LogModifier}
import scribe.writer.ConsoleWriter

case class Logger(parentName: Option[String] = Some(Logger.rootName),
                  modifiers: List[LogModifier] = Nil,
                  handlers: List[LogHandler] = Nil) extends LogSupport[Logger] with LoggerSupport {
  def orphan(): Logger = copy(parentName = None)
  def withParent(name: String): Logger = copy(parentName = Some(name))
  def withHandler(handler: LogHandler): Logger = copy(handlers = handlers ::: List(handler))
  def withoutHandler(handler: LogHandler): Logger = copy(handlers = handlers.filterNot(_ == handler))
  def clearHandlers(): Logger = copy(handlers = Nil)
  override def withModifier(modifier: LogModifier): Logger = copy(modifiers = modifiers ::: List(modifier))
  override def withoutModifier(modifier: LogModifier): Logger = copy(modifiers = modifiers.filterNot(_ == modifier))

  override def log(record: LogRecord): Unit = {
    modifiers.foldLeft(Option(record))((r, lm) => r.flatMap(lm.apply)).foreach { r =>
      handlers.foreach(_.log(r))
      parentName.map(Logger.byName).foreach(_.log(record))
    }
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

  def root: Logger = byName(rootName)

  private var map = Map.empty[String, Logger]

  // Configure the root logger to filter anything under Info and write to the console
  update(rootName)(
    _.orphan()
     .withModifier(LevelFilter >= Level.Info)
     .withHandler(LogHandler.default.withWriter(ConsoleWriter))
  )

  def byName(name: String): Logger = synchronized {
    val n = name.replaceAll("[$]", "")
    map.get(n) match {
      case Some(logger) => logger
      case None => {
        val logger = Logger(Some(rootName), Nil, Nil)
        map += n -> logger
        logger
      }
    }
  }

  def update(name: String, logger: Logger): Logger = synchronized {
    map += name -> logger
    logger
  }

  def update(name: String)(modifier: Logger => Logger): Logger = {
    update(name, modifier(byName(name)))
  }
}