package scribe.data

import scribe.util.Time

import scala.language.implicitConversions

object MDC {
  implicit lazy val global: MDC = creator(None)

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

  var manager: MDCManager = MDCThreadLocal
  var creator: Option[MDC] => MDC = parent => new MDCMap(parent)

  def instance: MDC = manager.instance

  def set(mdc: MDC): Unit = manager.instance = mdc
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
  def update(key: String, value: => Any): Unit = instance(key) = value
  def contextualize[Return](key: String, value: => Any)(f: => Return): Return = instance.contextualize(key, value)(f)
  def elapsed(key: String = "elapsed", timeFunction: () => Long = Time.function): Unit = instance.elapsed(key, timeFunction)
  def remove(key: String): Unit = instance.remove(key)
  def contains(key: String): Boolean = instance.contains(key)
  def clear(): Unit = instance.clear()
}

trait MDC {
  def map: Map[String, () => Any]
  def get(key: String): Option[() => Any]
  def update(key: String, value: => Any): Unit
  def contextualize[Return](key: String, value: => Any)(f: => Return): Return
  def elapsed(key: String, timeFunction: () => Long = Time.function): Unit
  def remove(key: String): Unit
  def contains(key: String): Boolean
  def clear(): Unit
}