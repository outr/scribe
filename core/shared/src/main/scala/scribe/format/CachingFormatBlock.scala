package scribe.format

import scribe.LogRecord
import scribe.output.LogOutput

/**
 * Adds time-based caching of output to reduce LogOutput cost
 */
trait CachingFormatBlock extends FormatBlock {
  /**
   * The amount of time in milliseconds to cache each generation of the LogOutput
   */
  protected def cacheLength: Long

  private lazy val cache = new ThreadLocal[LogOutput]
  private lazy val lastTimeStamp = new ThreadLocal[Long] {
    override def initialValue(): Long = 0L
  }

  override final def format(record: LogRecord): LogOutput = {
    val timeStamp = record.timeStamp
    if (timeStamp - lastTimeStamp.get() > cacheLength) {
      val output = formatCached(record)
      cache.set(output)
      lastTimeStamp.set(timeStamp)
      output
    } else {
      cache.get()
    }
  }

  protected def formatCached(record: LogRecord): LogOutput
}