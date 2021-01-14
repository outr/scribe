package scribe.handler

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong

import scribe.LogRecord
import scribe.format.Formatter
import scribe.modify.LogModifier
import scribe.output.format.OutputFormat
import scribe.writer.{ConsoleWriter, Writer}

import scala.language.implicitConversions

/**
  * Provides support for asynchronous logging to process the log record in another thread and avoid any blocking.
  *
  * @param formatter the formatter to use (defaults to Formatter.default)
  * @param writer the writer to use (defaults to ConsoleWriter)
  * @param outputFormat the output format to use (defaults to OutputFormat.default)
  * @param modifiers the modifiers
  * @param maxBuffer the maximum buffer before overflow occurs (defaults to AsynchronousLogHandler.DefaultMaxBuffer)
  * @param overflow what to do with overflows (defaults to DropOld)
  */
case class AsynchronousLogHandler(formatter: Formatter = Formatter.default,
                                  writer: Writer = ConsoleWriter,
                                  outputFormat: OutputFormat = OutputFormat.default,
                                  modifiers: List[LogModifier] = Nil,
                                  maxBuffer: Int = AsynchronousLogHandler.DefaultMaxBuffer,
                                  overflow: Overflow = Overflow.DropOld) extends LogHandler {
  private lazy val cached = new AtomicLong(0L)

  private lazy val queue = {
    val q = new ConcurrentLinkedQueue[LogRecord[_]]
    val t = new Thread {
      setDaemon(true)

      override def run(): Unit = while (true) {
        Option(q.poll()) match {
          case Some(record) => {
            cached.decrementAndGet()
            SynchronousLogHandler.log(modifiers, formatter, writer, record, outputFormat)
            Thread.sleep(1L)
          }
          case None => Thread.sleep(10L)
        }
      }
    }
    t.start()
    q
  }

  def withMaxBuffer(maxBuffer: Int): AsynchronousLogHandler = copy(maxBuffer = maxBuffer)

  def withOverflow(overflow: Overflow): AsynchronousLogHandler = copy(overflow = overflow)

  def withFormatter(formatter: Formatter): AsynchronousLogHandler = copy(formatter = formatter)

  def withWriter(writer: Writer): AsynchronousLogHandler = copy(writer = writer)

  def setModifiers(modifiers: List[LogModifier]): AsynchronousLogHandler = copy(modifiers = modifiers.sorted)

  override def log[M](record: LogRecord[M]): Unit = {
    val add = if (!cached.incrementIfLessThan(maxBuffer)) {
      overflow match {
        case Overflow.DropOld => {
          queue.poll()
          true
        }
        case Overflow.DropNew => false
        case Overflow.Block => {
          while(!cached.incrementIfLessThan(maxBuffer)) {
            Thread.sleep(1L)
          }
          true
        }
        case Overflow.Error => throw new LogOverflowException(s"Queue filled (max: $maxBuffer) while attempting to asynchronously log")
      }
    } else {
      true
    }
    if (add) {
      queue.add(record)
    }
  }
}

object AsynchronousLogHandler {
  /**
    * The default max buffer of log records (set to 1000)
    */
  val DefaultMaxBuffer: Int = 1000
}