package scribe.writer.file

import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

// TODO: Support FlushMode with this as one of the implementations
class AsynchronousFlusher(logFile: LogFile, delay: Long) {
  private val flushing = new AtomicBoolean(false)
  private val dirty = new AtomicBoolean(false)
  private val lastFlush = new AtomicLong(0L)

  def written(): Unit = if(flushing.compareAndSet(false, true)) {
    flush()
  } else {
    dirty.set(true)
  }

  // TODO: avoid using the global ExecutionContext
  private def flush(): Unit = Future {
    try {
      val delay = this.delay - (System.currentTimeMillis() - lastFlush.get())
      if (delay > 0L) {
        Thread.sleep(delay)
      }
      logFile.flush()
    } finally {
      lastFlush.set(System.currentTimeMillis())
      if (dirty.compareAndSet(true, false)) {
        flush()
      } else {
        flushing.set(false)
      }
    }
  }
}