package scribe.handler

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong

import scribe.{LogRecord, handler}
import scribe.format.Formatter
import scribe.modify.LogModifier
import scribe.writer.{ConsoleWriter, Writer}

import scala.language.implicitConversions

case class AsynchronousLogHandler(formatter: Formatter = Formatter.default,
                                  writer: Writer = ConsoleWriter,
                                  modifiers: List[LogModifier] = Nil,
                                  overflow: Overflow = Overflow.DropOld) extends LogHandler {
  def withOverflow(overflow: Overflow): AsynchronousLogHandler = copy(overflow = overflow)

  override def withFormatter(formatter: Formatter): AsynchronousLogHandler = copy(formatter = formatter)

  override def withWriter(writer: Writer): AsynchronousLogHandler = copy(writer = writer)

  override def setModifiers(modifiers: List[LogModifier]): AsynchronousLogHandler = copy(modifiers = modifiers)

  override def log[M](record: LogRecord[M]): Unit = {
    val add = if (!AsynchronousLogHandler.cached.incrementIfLessThan(AsynchronousLogHandler.maxBuffer)) {
      overflow match {
        case Overflow.DropOld => {
          AsynchronousLogHandler.queue.poll()
          true
        }
        case Overflow.DropNew => false
        case Overflow.Block => {
          while(!AsynchronousLogHandler.cached.incrementIfLessThan(AsynchronousLogHandler.maxBuffer)) {
            Thread.sleep(1L)
          }
          true
        }
        case Overflow.Error => throw new LogOverflowException(s"Queue filled (max: ${AsynchronousLogHandler.maxBuffer}) while attempting to asynchronously log")
      }
    } else {
      true
    }
    if (add) {
      AsynchronousLogHandler.queue.add(new handler.AsynchronousLogHandler.AsynchronousLogRecord(this, record))
    }
  }

  implicit def atomicExtras(l: AtomicLong): AtomicLongExtras = new AtomicLongExtras(l)
}

object AsynchronousLogHandler {
  val DefaultMaxBuffer: Int = 1000

  var maxBuffer: Int = DefaultMaxBuffer

  private lazy val cached = new AtomicLong(0L)

  private lazy val queue = {
    val q = new ConcurrentLinkedQueue[AsynchronousLogRecord]
    val t = new Thread {
      setDaemon(true)

      override def run(): Unit = while (true) {
        Option(q.poll()) match {
          case Some(record) => {
            cached.decrementAndGet()
            SynchronousLogHandler.log(record.handler, record.record)
            Thread.sleep(10L)
          }
          case None => Thread.sleep(10L)
        }
      }
    }
    t.start()
    q
  }

  private class AsynchronousLogRecord(val handler: AsynchronousLogHandler, val record: LogRecord[_])
}