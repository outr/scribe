package scribe.format

import java.lang

import scribe._

trait FormatBlock {
  def format(record: LogRecord, b: java.lang.StringBuilder): Unit
}

object FormatBlock {
  case class RawString(s: String) extends FormatBlock {
    override def format(record: LogRecord, b: java.lang.StringBuilder): Unit = b.append(s)
  }

  object Date {
    object Standard extends FormatBlock {
      private lazy val cache = new ThreadLocal[String] {
        override def initialValue(): String = ""
      }
      private lazy val lastValue = new ThreadLocal[Long] {
        override def initialValue(): Long = 0L
      }

      override def format(record: LogRecord, b: java.lang.StringBuilder): Unit = {
        val l = record.timeStamp
        val current = if (l - lastValue.get() > 1000L) {
          val d = sfi"${l.tY}.${l.tm}.${l.td} ${l.tT}"
          cache.set(d)
          lastValue.set(l)
          d
        } else {
          cache.get()
        }
        b.append(current)
      }
    }
  }

  object ThreadName extends FormatBlock {
    override def format(record: LogRecord, b: java.lang.StringBuilder): Unit = b.append(record.thread.getName)
  }

  object Level {
    object PaddedRight extends FormatBlock {
      override def format(record: LogRecord, b: java.lang.StringBuilder): Unit = b.append(record.level.namePaddedRight)
    }
  }

  object ClassName {
    object Full extends FormatBlock {
      override def format(record: LogRecord, b: lang.StringBuilder): Unit = {
        b.append(record.className)
      }
    }
    object Abbreviated extends FormatBlock {
      private val MaxSize = 1000000
      private var cache = Map.empty[String, String]

      override def format(record: LogRecord, b: java.lang.StringBuilder): Unit = {
        val abbreviation: String = cache.get(record.className) match {
          case Some(a) => a
          case None => synchronized {
            val parts = record.className.split('.')
            val last = parts.length - 1
            val a = parts.zipWithIndex.map {
              case (cur, i) if i == last => cur
              case (cur, _) => cur.head
            }.mkString(".")
            if (cache.size >= MaxSize) {
              cache = Map.empty
            }
            cache += record.className -> a
            a
          }
        }
        b.append(abbreviation)
      }
    }
  }

  object MethodName {
    object Full extends FormatBlock {
      override def format(record: LogRecord, b: lang.StringBuilder): Unit = {
        record.methodName.foreach(b.append)
      }
    }
  }

  object Position {
    object Full extends FormatBlock {
      override def format(record: LogRecord, b: lang.StringBuilder): Unit = {
        ClassName.Full.format(record, b)
        if (record.methodName.nonEmpty) {
          b.append(".")
          MethodName.Full.format(record, b)
        }
        if (record.lineNumber.nonEmpty) {
          b.append(":")
          LineNumber.Full.format(record, b)
        }
      }
    }
    object Abbreviated extends FormatBlock {
      override def format(record: LogRecord, b: lang.StringBuilder): Unit = {
        ClassName.Abbreviated.format(record, b)
        if (record.methodName.nonEmpty) {
          b.append(".")
          MethodName.Full.format(record, b)
        }
        if (record.lineNumber.nonEmpty) {
          b.append(":")
          LineNumber.Full.format(record, b)
        }
      }
    }
  }

  object LineNumber {
    object Full extends FormatBlock {
      override def format(record: LogRecord, b: lang.StringBuilder): Unit = {
        record.lineNumber.foreach(b.append)
      }
    }
  }

  object Message extends FormatBlock {
    override def format(record: LogRecord, b: java.lang.StringBuilder): Unit = b.append(record.message)
  }

  object NewLine extends FormatBlock {
    override def format(record: LogRecord, b: java.lang.StringBuilder): Unit = b.append("\n")
  }
}