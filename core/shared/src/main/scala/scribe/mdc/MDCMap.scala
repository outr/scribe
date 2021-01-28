package scribe.mdc

import scribe.util.Time

class MDCMap(parent: Option[MDC]) extends MDC {
  private var _map: Map[String, () => Any] = Map.empty

  override def map: Map[String, () => Any] = _map

  override def get(key: String): Option[() => Any] = _map.get(key).orElse(parent.flatMap(_.get(key)))

  override def update(key: String, value: => Any): Unit = _map = _map + (key -> (() => value))

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
    import perfolation._
    update(key, s"${((timeFunction() - start) / 1000.0).f()}s")
  }

  override def remove(key: String): Unit = _map = _map - key

  override def contains(key: String): Boolean = map.contains(key)

  override def clear(): Unit = _map = Map.empty
}