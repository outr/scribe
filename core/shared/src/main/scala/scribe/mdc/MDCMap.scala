package scribe.mdc

import scribe.util.Time
import perfolation._

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._

class MDCMap(parent: Option[MDC]) extends MDC {
  private val _map = new ConcurrentHashMap[String, () => Any]

  override def map: Map[String, () => Any] = _map.asScala.toMap

  override def get(key: String): Option[() => Any] = Option(_map.get(key)).orElse(parent.flatMap(_.get(key)))

  override def update(key: String, value: => Any): Unit = _map.put(key, () => value)

  override def contextualize[Return](key: String, value: => Any)(f: => Return): Return = {
    update(key, value)
    try {
      f
    } finally {
      remove(key)
    }
  }

  override def elapsed(key: String, timeFunction: () => Long = Time.function): Unit = {
    val start = timeFunction()
    update(key, s"${((timeFunction() - start) / 1000.0).f()}s")
  }

  override def remove(key: String): Unit = _map.remove(key)

  override def contains(key: String): Boolean = map.contains(key)

  override def clear(): Unit = _map.clear()
}