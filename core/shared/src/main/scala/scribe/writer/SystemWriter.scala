package scribe.writer

import scribe.output._
import scribe.output.format.OutputFormat
import scribe.{ANSI, Level, LogRecord, Logger}

import java.io.PrintStream
import scala.math.Ordering.Implicits._
import scala.language.implicitConversions

/**
 * SystemWriter writes to System.out or System.err choosing the latter if the level is higher than Info
 */
object SystemWriter extends Writer {
  /**
    * If true, will always synchronize writing to the console to avoid interleaved text. Most native consoles will
    * handle this automatically, but IntelliJ and Eclipse are notorious about not properly handling this.
    * Defaults to true.
    */
  var synchronizeWriting: Boolean = true

  /**
   * Workaround for some consoles that don't play nicely with asynchronous calls
   */
  var alwaysFlush: Boolean = false

  val DefaultStringBuilderStartCapacity: Int = 512

  var stringBuilderStartCapacity: Int = DefaultStringBuilderStartCapacity

  private val stringBuilders = new ThreadLocal[StringBuilder] {
    override def initialValue(): StringBuilder = new StringBuilder(stringBuilderStartCapacity)
  }

  override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = {
    val stream = if (record.level <= Level.Info) {
      Logger.system.out
    } else {
      Logger.system.err
    }
    write(stream, output, outputFormat)
  }

  def write[M](stream: PrintStream, output: LogOutput, outputFormat: OutputFormat): Unit = {
    val sb = stringBuilders.get()
    outputFormat.begin(sb.append(_))
    outputFormat(output, s => sb.append(s))
    outputFormat.end(sb.append(_))
    if (synchronizeWriting) {
      synchronized {
        stream.println(sb.toString())
        if (alwaysFlush) stream.flush()
      }
    } else {
      stream.println(sb.toString())
      if (alwaysFlush) stream.flush()
    }
    sb.clear()
  }
}