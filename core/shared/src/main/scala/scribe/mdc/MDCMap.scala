package scribe.mdc

import scribe.util.Time
import perfolation._

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._

class MDCMap(parent: Option[MDC]) extends MDC {
  private val _map = new ConcurrentHashMap[String, () => Any]

  override def map: Map[String, () => Any] = _map.asScala.toMap

  override def get(key: String): Option[() => Any] = Option(_map.get(key)).orElse(parent.flatMap(_.get(key)))

  override def update(key: String, value: => Any): Option[Any] = Option(_map.put(key, () => value)).map(_())

  override def set(key: String, value: Option[Any]): Option[Any] = value match {
    case Some(v) => update(key, v)
    case None => remove(key)
  }

  override def context[Return](values: (String, MDCValue)*)(f: => Return): Return = {
    val previous = values.map {
      case (key, value) => key -> update(key, value.value())
    }
    try {
      f
    } finally {
      previous.foreach {
        case (key, value) => set(key, value)
      }
    }
  }

  override def remove(key: String): Option[Any] = Option(_map.remove(key)).map(_())

  override def contains(key: String): Boolean = map.contains(key)

  override def clear(): Unit = _map.clear()
}