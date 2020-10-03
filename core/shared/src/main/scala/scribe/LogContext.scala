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
  @volatile private var references = 0

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

  def release(): Unit = synchronized {
    if (references > 0) {
      references -= 1
    } else {
      LogContext.synchronized {
        LogContext.instances = this :: LogContext.instances
      }
    }
  }

  def retain(): Unit = synchronized {
    references += 1
  }
}