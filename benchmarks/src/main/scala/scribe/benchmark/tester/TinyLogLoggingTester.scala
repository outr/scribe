package scribe.benchmark.tester

import org.pmw.tinylog

class TinyLogLoggingTester extends LoggingTester {
  override def init(): Unit = tinylog.Configurator
    .defaultConfig()
    .removeAllWriters()
    .writer(new tinylog.writers.FileWriter("logs/tiny.log"))
    .level(tinylog.Level.INFO)
    .activate()

  override def run(messages: Iterator[String]): Unit = {
    messages.foreach(tinylog.Logger.info)
  }
}