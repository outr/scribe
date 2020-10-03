package scribe

import scribe.modify.LogModifier

object LogContext {
  private var instances = List.empty[LogContext]

  def apply(): LogContext = synchronized {
    instances.headOption match {
      case Some(i) => {
        instances = instances.tail
        i.set = Set.empty
        i
      }
      case None => new LogContext
    }
  }
}

class LogContext {
  private var set = Set.empty[String]

  def run[M](record: LogRecord[M], modifier: LogModifier): Option[LogRecord[M]] = if (shouldRunModifier(modifier)) {
    modifier(record)
  } else {
    Some(record)
  }

  def shouldRunModifier(modifier: LogModifier): Boolean = synchronized {
    if (set.contains(modifier.id)) {
      false
    } else {
      set += modifier.id
      true
    }
  }

  def release(): Unit = LogContext.synchronized {
    LogContext.instances = this :: LogContext.instances
  }
}