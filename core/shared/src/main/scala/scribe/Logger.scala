package scribe2

case class Logger(parentName: Option[String],
                  modifiers: List[LogModifier],
                  handlers: List[LogHandler]) extends LogHandler {
  override def log(record: LogRecord): Unit = {
    modifiers.foldLeft(Option(record))((r, lm) => r.flatMap(lm.apply)).foreach { r =>
      handlers.foreach(_.log(r))
      parentName.map(Logger.byName).foreach(_.log(record))
    }
  }
}

object Logger {
  val rootName: String = "root"

  def root: Logger = byName(rootName)

  private var map = Map.empty[String, Logger]

  update(rootName, Logger(None, Nil, Nil))

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

  def update(name: String, logger: Logger): Unit = synchronized {
    map += name -> logger
  }

  def update(name: String)(modifier: Logger => Logger): Unit = {
    update(name, modifier(byName(name)))
  }
}