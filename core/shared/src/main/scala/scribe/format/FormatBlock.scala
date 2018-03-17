package scribe.format

import scribe._
import perfolation._

trait FormatBlock {
  def format[M](record: LogRecord[M]): String

  def map(f: String => String): FormatBlock = FormatBlock.Mapped(this, f)
}

object FormatBlock {
  def apply(f: LogRecord[_] => String): FormatBlock = new FormatBlock {
    override def format[M](record: LogRecord[M]): String = f(record)
  }

  case class Mapped(block: FormatBlock, f: String => String) extends FormatBlock {
    override def format[M](record: LogRecord[M]): String = f(block.format(record))
  }

  case class RawString(s: String) extends FormatBlock {
    override def format[M](record: LogRecord[M]): String = s
  }

  object Date {
    object Standard extends FormatBlock {
      private lazy val cache = new ThreadLocal[String] {
        override def initialValue(): String = ""
      }
      private lazy val lastValue = new ThreadLocal[Long] {
        override def initialValue(): Long = 0L
      }

      override def format[M](record: LogRecord[M]): String = {
        val l = record.timeStamp
        if (l - lastValue.get() > 1000L) {
          val d = p"${l.t.Y}.${l.t.m}.${l.t.d} ${l.t.T}"
          cache.set(d)
          lastValue.set(l)
          d
        } else {
          cache.get()
        }
      }
    }
  }

  object ThreadName extends FormatBlock {
    override def format[M](record: LogRecord[M]): String = record.thread.getName
  }

  object Level {
    object PaddedRight extends FormatBlock {
      override def format[M](record: LogRecord[M]): String = record.level.namePaddedRight
    }
  }

  object ClassName {
    object Full extends FormatBlock {
      override def format[M](record: LogRecord[M]): String = record.className
    }
    object Abbreviated extends FormatBlock {
      private val MaxSize = 1000000
      private var cache = Map.empty[String, String]

      override def format[M](record: LogRecord[M]): String = {
        cache.get(record.className) match {
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
      }
    }
  }

  object MethodName {
    object Full extends FormatBlock {
      override def format[M](record: LogRecord[M]): String = record.methodName.getOrElse("")
    }
  }

  object Position {
    object Full extends FormatBlock {
      override def format[M](record: LogRecord[M]): String = {
        val className = ClassName.Full.format(record)
        val methodName = if (record.methodName.nonEmpty) {
          p".${MethodName.Full.format(record)}"
        } else {
          ""
        }
        val lineNumber = if (record.lineNumber.nonEmpty) {
          p":${LineNumber.Full.format(record)}"
        } else {
          ""
        }
        p"$className$methodName$lineNumber"
      }
    }
    object Abbreviated extends FormatBlock {
      override def format[M](record: LogRecord[M]): String = {
        val className = ClassName.Abbreviated.format(record)
        val methodName = if (record.methodName.nonEmpty) {
          p".${MethodName.Full.format(record)}"
        } else {
          ""
        }
        val lineNumber = if (record.lineNumber.nonEmpty) {
          p":${LineNumber.Full.format(record)}"
        } else {
          ""
        }
        p"$className$methodName$lineNumber"
      }
    }
  }

  object LineNumber {
    object Full extends FormatBlock {
      override def format[M](record: LogRecord[M]): String = record.lineNumber.map(_.toString).getOrElse("")
    }
  }

  object Message extends FormatBlock {
    override def format[M](record: LogRecord[M]): String = record.message
  }

  object NewLine extends FormatBlock {
    override def format[M](record: LogRecord[M]): String = "\n"
  }
}