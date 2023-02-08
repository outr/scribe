package scribe.handler

import scribe.LogRecord

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong

/**
 * CachingLogHandler provides a convenient LogHandler to cache LogRecords and drop old records if the record count
 * overflows.
 */
case class CachingLogHandler(maxBuffer: Int = CachingLogHandler.DefaultMaxBuffer) extends LogHandler {
  private lazy val cached = new AtomicLong(0L)
  private lazy val queue = new ConcurrentLinkedQueue[LogRecord]

  override def log(record: LogRecord): Unit = {
    if (!cached.incrementIfLessThan(maxBuffer)) {
      queue.poll()    // Drop oldest
    }
    queue.add(record)
  }

  def poll(): Option[LogRecord] = {
    val option = Option(queue.poll())
    if (option.nonEmpty) cached.decrementAndGet()
    option
  }

  def size: Long = cached.get()
}

object CachingLogHandler {
  /**
   * The default max buffer of log records (set to 1000)
   */
  val DefaultMaxBuffer: Int = 1000
}