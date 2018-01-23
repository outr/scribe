package scribe2

import java.io.PrintStream

case class Logger(parentName: Option[String],
                  modifiers: List[LogModifier],
                  handlers: List[LogHandler]) {
  def log(record: LogRecord): Unit = {
    modifiers.foldLeft(Option(record))((r, lm) => r.flatMap(lm.apply)).foreach { r =>
      handlers.foreach(_.log(r))
      parentName.map(Logger.byName).foreach(_.log(record))
    }
  }
}

object Logger {
  private val systemOut = System.out
  private val systemErr = System.err

  object system {
    def out: PrintStream = systemOut
    def err: PrintStream = systemErr
  }

  val rootName: String = "root"

  def root: Logger = byName(rootName)

  private var map = Map.empty[String, Logger]

  update(rootName, Logger(None, Nil, List(LogHandler(Formatter.default, ConsoleWriter, Nil))))

  def byName(name: String): Logger = synchronized {
    map.get(name) match {
      case Some(logger) => logger
      case None => {
        val logger = Logger(Some(rootName), Nil, Nil)
        map += name -> logger
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