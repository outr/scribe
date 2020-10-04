package scribe

import scribe.util.Time

object MDC {
  lazy val global: MDC = new MDC(None)

  private val threadLocal: InheritableThreadLocal[MDC] = new InheritableThreadLocal[MDC] {
    override def initialValue(): MDC = new MDC(Some(global))

    override def childValue(parentValue: MDC): MDC = new MDC(Option(parentValue).orElse(Some(global)))
  }
  def instance: MDC = threadLocal.get()

  def set(mdc: MDC): Unit = threadLocal.set(mdc)
  def contextualize[Return](mdc: MDC)(f: => Return): Return = {
    val previous = threadLocal.get()
    set(mdc)
    try {
      f
    } finally {
      set(previous)
    }
  }

  def map: Map[String, () => String] = instance.map
  def get(key: String): Option[String] = instance.get(key).map(_())
  def update(key: String, value: => String): Unit = instance(key) = value
  def contextualize[Return](key: String, value: => String)(f: => Return): Return = instance.contextualize(key, value)(f)
  def elapsed(key: String, timeFunction: () => Long = Time.function): Unit = instance.elapsed(key, timeFunction)
  def remove(key: String): Unit = instance.remove(key)
  def clear(): Unit = instance.clear()
}

class MDC(parent: Option[MDC]) {
  private var _map: Map[String, () => String] = Map.empty

  def map: Map[String, () => String] = _map

  def get(key: String): Option[() => String] = _map.get(key).orElse(parent.flatMap(_.get(key)))

  def update(key: String, value: => String): Unit = _map = _map + (key -> (() => value))

  def contextualize[Return](key: String, value: => String)(f: => Return): Return = {
    update(key, value)
    try {
      f
    } finally{
      remove(key)
    }
  }

  def elapsed(key: String, timeFunction: () => Long = Time.function): Unit = {
    val start = timeFunction()
    import perfolation._
    update(key, s"${((timeFunction() - start) / 1000.0).f()} seconds elapsed")
  }

  def remove(key: String): Unit = _map = _map - key

  def clear(): Unit = _map = Map.empty
}