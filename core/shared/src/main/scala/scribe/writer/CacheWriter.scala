package scribe.writer
import scribe.LogRecord
import scribe.output.LogOutput
import scribe.output.format.OutputFormat

class CacheWriter(max: Int = CacheWriter.DefaultMax) extends Writer {
  private var recordCache = List.empty[LogRecord]
  private var outputCache = List.empty[LogOutput]

  override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = synchronized {
    recordCache = (record :: recordCache).take(max)
    outputCache = (output :: outputCache).take(max)
  }

  def records: List[LogRecord] = recordCache
  def output: List[LogOutput] = outputCache

  def consume[Return](f: List[LogRecord] => Return): Return = try {
    f(records)
  } finally {
    clear()
  }

  def consumeMessages[Return](f: List[String] => Return): Return = consume { list =>
    f(list.map(_.messages.map(_.logOutput.plainText).mkString(" ")))
  }

  def clear(): Unit = synchronized {
    recordCache = Nil
    outputCache = Nil
  }
}

object CacheWriter {
  val DefaultMax: Int = 100
}