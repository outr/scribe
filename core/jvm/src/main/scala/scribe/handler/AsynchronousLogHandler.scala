package scribe.handler

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong

import scribe.{LogContext, LogRecord}
import scribe.format.Formatter
import scribe.modify.LogModifier
import scribe.writer.{ConsoleWriter, Writer}
import perfolation._

import scala.language.implicitConversions

case class AsynchronousLogHandler(formatter: Formatter = Formatter.default,
                                  writer: Writer = ConsoleWriter,
                                  modifiers: List[LogModifier] = Nil,
                                  maxBuffer: Int = AsynchronousLogHandler.DefaultMaxBuffer,
                                  overflow: Overflow = Overflow.DropOld) extends LogHandler {
  private lazy val cached = new AtomicLong(0L)

  private lazy val queue = {
    val q = new ConcurrentLinkedQueue[(LogRecord[_], LogContext)]
    val t = new Thread {
      setDaemon(true)

      override def run(): Unit = while (true) {
        Option(q.poll()) match {
          case Some((record, context)) => {
            try {
              cached.decrementAndGet()
              SynchronousLogHandler.log(modifiers, formatter, writer, record)
            } finally {
              context.release()
            }
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

  def setModifiers(modifiers: List[LogModifier]): AsynchronousLogHandler = copy(modifiers = modifiers)

  override def log[M](record: LogRecord[M], context: LogContext): Unit = {
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
        case Overflow.Error => throw new LogOverflowException(p"Queue filled (max: $maxBuffer) while attempting to asynchronously log")
      }
    } else {
      true
    }
    if (add) {
      context.retain()
      queue.add((record, context))
    }
  }
}

object AsynchronousLogHandler {
  val DefaultMaxBuffer: Int = 1000
}