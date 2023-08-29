import scribe.LogFeature
import scribe.mdc.MDC
import sourcecode.{FileName, Line, Name, Pkg}

import scala.language.experimental.macros
import scala.language.implicitConversions

package object scribe extends LoggerSupport[Unit] {
  lazy val lineSeparator: String = System.getProperty("line.separator")

  protected[scribe] var disposables = Set.empty[() => Unit]

  @inline
  override final def log(record: LogRecord): Unit = Logger(record.className).log(record)

  override def log(level: Level, mdc: MDC, features: LogFeature*)
                  (implicit pkg: Pkg, fileName: FileName, name: Name, line: Line): Unit =
    if (includes(level)) super.log(level, mdc, features: _*)

  def includes(level: Level)(implicit pkg: sourcecode.Pkg,
                             fileName: sourcecode.FileName,
                             name: sourcecode.Name,
                             line: sourcecode.Line): Boolean = {
    val (_, className) = LoggerSupport.className(pkg, fileName)
    Logger(className).includes(level)
  }

  /**
   * LogFeature convenience functionality to set data on a log
   */
  def data(key: String, value: => Any): LogFeature = LogFeature(_(key) = () => value)

  /**
   * LogFeature convenience functionality to set a map of data on a log
   */
  def data(map: Map[String, Any]): LogFeature = LogFeature { r =>
    map.foldLeft(r)((record, tuple) => record(tuple._1) = () => tuple._2)
  }

  /**
   * LogFeature convenience functionality to set a booster on a log
   */
  def boost(booster: Double => Double): LogFeature = LogFeature(_.boost(booster))

  /**
   * LogFeature convenience functionality to override the default thread on a log
   */
  def thread(thread: Thread): LogFeature = LogFeature(_.copy(thread = thread))

  /**
   * LogFeature convenience functionality to override the timeStamp on a log
   */
  def timeStamp(timeStamp: Long): LogFeature = LogFeature(_.copy(timeStamp = timeStamp))

  def dispose(): Unit = disposables.foreach(d => d())

  implicit def level2Double(level: Level): Double = level.value

  implicit class AnyLogging(value: Any) {
    def logger: Logger = Logger(value.getClass.getName)
  }
}