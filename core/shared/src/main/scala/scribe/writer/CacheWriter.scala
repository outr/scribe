package scribe.writer
import scribe.LogRecord
import scribe.output.LogOutput
import scribe.output.format.OutputFormat

class CacheWriter(max: Int = CacheWriter.DefaultMax) extends Writer {
  private var recordCache = List.empty[LogRecord[Any]]
  private var outputCache = List.empty[LogOutput]

  override def write[M](record: LogRecord[M], output: LogOutput, outputFormat: OutputFormat): Unit = synchronized {
    recordCache = (record.asInstanceOf[LogRecord[Any]] :: recordCache).take(max)
    outputCache = (output :: outputCache).take(max)
  }

  def records: List[LogRecord[Any]] = recordCache
  def output: List[LogOutput] = outputCache

  def clear(): Unit = synchronized {
    recordCache = Nil
    outputCache = Nil
  }
}

object CacheWriter {
  val DefaultMax: Int = 100
}