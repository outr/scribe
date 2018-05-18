package scribe.handler

import java.util.concurrent.atomic.AtomicLong

import scala.annotation.tailrec

class AtomicLongExtras(val value: AtomicLong) extends AnyVal {
  /**
    * Modifies the value atomicly without locking if the resulting value of the function is Some.
    */
  @tailrec
  final def modify(f: Long => Option[Long]): Boolean = {
    val current = value.get()
    f(current) match {
      case Some(v) => if (value.compareAndSet(current, v)) {
        true
      }
      else {
        modify(f)
      }
      case None => false
    }
  }

  /**
    * Increments and returns the new value
    */
  def ++ : Long = value.incrementAndGet()

  /**
    * Decrements and returns the new value
    */
  def -- : Long = value.decrementAndGet()

  /**
    * Adds the value and returns the new value
    */
  def +=(value: Long): Long = this.value.addAndGet(value)

  /**
    * Subtracts the value and returns the new value
    */
  def -=(value: Long): Long = this.value.addAndGet(-value)

  /**
    * Increments the value if the current value is less than the max value supplied.
    *
    * This method is thread-safe without locking.
    */
  def incrementIfLessThan(max: Int): Boolean = modify((value: Long) => {
    if (value < max) {
      Some(value + 1)
    }
    else {
      None
    }
  })

  /**
    * Decrements the value if the current value is greater than the max value supplied.
    *
    * This method is thread-safe without locking.
    */
  def decrementIfGreaterThan(min: Long): Boolean = modify((value: Long) => {
    if (value > min) {
      Some(value - 1)
    }
    else {
      None
    }
  })
}