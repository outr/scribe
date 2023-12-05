package scribe.mdc

import perfolation.double2Implicits
import scribe.util.Time

import scala.language.implicitConversions

object MDC {
  /**
   * Global MDC instance. By default, all instances of MDC inherit from this.
   */
  lazy val global: MDC = creator(None)

  /**
   * The MDCManager responsible for retrieving an MDC instance for the context. By default this uses MDCThreadLocal but
   * can replaced with something more advanced.
   */
  var manager: MDCManager = MDCThreadLocal

  /**
   * The function to create MDC instances. Receives the parent and creates a new MDC instance. By default, this will use
   * MDCMap, but this can be replaced to provide a different implementation.
   */
  var creator: Option[MDC] => MDC = parent => new MDCMap(parent)

  def apply[Return](f: MDC => Return): Return = {
    val previous = manager.instance
    val mdc = new MDCMap(Some(previous))
    try {
      manager.instance = mdc
      f(mdc)
    } finally {
      manager.instance = previous
    }
  }

  /**
   * Convenience implicit to get the current instance of MDC from the manager
   */
  implicit def instance: MDC = manager.instance

  /**
   * Sets the instance for the current context
   */
  def set(mdc: MDC): Unit = manager.instance = mdc

  /**
   * Sets the instance for the current context for the duration of the function `f`.
   */
  def contextualize[Return](mdc: MDC)(f: => Return): Return = {
    val previous = manager.instance
    set(mdc)
    try {
      f
    } finally {
      set(previous)
    }
  }

  def map: Map[String, () => Any] = instance.map
  def get(key: String): Option[Any] = instance.get(key).map(_())
  def getOrElse(key: String, default: => Any): Any= get(key).getOrElse(default)
  def update(key: String, value: => Any): Option[Any] = instance(key) = value
  def set(key: String, value: Option[Any]): Option[Any] = instance.set(key, value)
  def context[Return](values: (String, MDCValue)*)(f: => Return): Return = instance.context(values: _*)(f)
  def elapsed(key: String = "elapsed", timeFunction: () => Long = Time.function): Unit = instance.elapsed(key, timeFunction)
  def remove(key: String): Option[Any] = instance.remove(key)
  def contains(key: String): Boolean = instance.contains(key)
  def clear(): Unit = instance.clear()
}

trait MDC {
  /**
   * Retrieves the functional map
   */
  def map: Map[String, () => Any]

  /**
   * Gets the value function for this key if set
   */
  def get(key: String): Option[() => Any]

  /**
   * Updates the value for the specified key. The `context` method should be preferred to avoid leaving MDC values set
   * forever. Returns the previous value for this key.
   */
  def update(key: String, value: => Any): Option[Any]

  /**
   * Sets the value for the specified key. This method differs from `update` by taking an `Option` that will remove the
   * key if set to `None`. Returns the previous value for this key.
   */
  def set(key: String, value: Option[Any]): Option[Any]

  /**
   * Contextualizes setting multiple values similar to `update`, but returns them to their previous value upon
   * completion of the context function `f`.
   */
  def context[Return](values: (String, MDCValue)*)(f: => Return): Return

  /**
   * Applies an elapsed function as an MDC value. This represents a dynamically changing value of time elapsed since the
   * this was set.
   */
  def elapsed(key: String, timeFunction: () => Long = Time.function): Unit = {
    val start = timeFunction()
    update(key, s"${((timeFunction() - start) / 1000.0).f()}s")
  }

  /**
   * Removes a key from this MDC instance
   */
  def remove(key: String): Option[Any]

  /**
   * True if this MDC contains the specified key
   */
  def contains(key: String): Boolean

  /**
   * Clears all values from this MDC
   */
  def clear(): Unit
}