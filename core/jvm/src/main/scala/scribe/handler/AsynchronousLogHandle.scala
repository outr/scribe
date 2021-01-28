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
case class AsynchronousLogHandle(maxBuffer: Int,
                                 overflow: Overflow = Overflow.DropOld) extends LogHandle {
  private lazy val cached = new AtomicLong(0L)

  private lazy val queue = {
    val q = new ConcurrentLinkedQueue[(LogHandlerBuilder, LogRecord[_])]
    val t = new Thread {
      setDaemon(true)

      override def run(): Unit = while (true) {
        Option(q.poll()) match {
          case Some((handler, record)) => {
            cached.decrementAndGet()
            SynchronousLogHandle.log(handler, record)
            Thread.sleep(1L)
          }
          case None => Thread.sleep(10L)
        }
      }
    }
    t.start()
    q
  }

  def withMaxBuffer(maxBuffer: Int): AsynchronousLogHandle = copy(maxBuffer = maxBuffer)

  def withOverflow(overflow: Overflow): AsynchronousLogHandle = copy(overflow = overflow)

  override def log[M](handler: LogHandlerBuilder, record: LogRecord[M]): Unit = {
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
      queue.add(handler -> record)
    }
  }
}

object AsynchronousLogHandle {
  /**
    * The default max buffer of log records (set to 1000)
    */
  val DefaultMaxBuffer: Int = 1000
}