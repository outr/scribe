package scribe

import scala.language.experimental.macros

trait LoggerSupport {
  def log(record: LogRecord): Unit

  def info(message: => Any): Unit = macro Macros.info
}