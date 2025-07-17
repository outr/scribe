package scribe.jpl

import java.util.concurrent.ConcurrentHashMap

class ScribeSystemLoggerFinder extends System.LoggerFinder {

  private lazy val loggers = new ConcurrentHashMap[String, System.Logger]()

  def getLogger(name: String, module: Module): System.Logger =
    loggers.computeIfAbsent(name, n => new ScribeSystemLogger(n))

}
