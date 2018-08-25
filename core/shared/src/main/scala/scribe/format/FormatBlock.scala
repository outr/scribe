package scribe.format

import scribe._
import perfolation._
import scribe.util.Abbreviator

trait FormatBlock {
  def format[M](record: LogRecord[M]): String

  def map(f: String => String): FormatBlock = FormatBlock.Mapped(this, f)

  def abbreviate(maxLength: Int,
                 padded: Boolean = false,
                 separator: Char = '.',
                 removeEntries: Boolean = true,
                 abbreviateName: Boolean = false): FormatBlock = {
    val block = new AbbreviateBlock(this, maxLength, separator, removeEntries, abbreviateName)
    if (padded) {
      new RightPaddingBlock(block, maxLength, ' ')
    } else {
      block
    }
  }

  def padRight(length: Int, padding: Char = ' '): FormatBlock = new RightPaddingBlock(this, length, padding)
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

  object TimeStamp extends FormatBlock {
    override def format[M](record: LogRecord[M]): String = record.timeStamp.toString
  }

  object Time extends FormatBlock {
    override def format[M](record: LogRecord[M]): String = p"${record.timeStamp.t.T}"
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

  object Level extends FormatBlock {
    override def format[M](record: LogRecord[M]): String = record.level.name

    object PaddedRight extends FormatBlock {
      override def format[M](record: LogRecord[M]): String = record.level.namePaddedRight
    }
  }

  object FileName extends FormatBlock {
    override def format[M](record: LogRecord[M]): String = record.fileName
  }

  object ClassName extends FormatBlock {
    override def format[M](record: LogRecord[M]): String = record.className
  }

  object MethodName extends FormatBlock {
    override def format[M](record: LogRecord[M]): String = record.methodName.getOrElse("")
  }

  object ClassAndMethodName extends FormatBlock {
    override def format[M](record: LogRecord[M]): String = {
      val className = ClassName.format(record)
      val methodName = if (record.methodName.nonEmpty) {
        p".${MethodName.format(record)}"
      } else {
        ""
      }
      p"$className$methodName"
    }
  }

  object Position extends FormatBlock {
    override def format[M](record: LogRecord[M]): String = {
      val lineNumber = if (record.lineNumber.nonEmpty) {
        p":${LineNumber.format(record)}"
      } else {
        ""
      }
      p"${ClassAndMethodName.format(record)}$lineNumber"
    }

    override def abbreviate(maxLength: Int,
                            padded: Boolean = false,
                            separator: Char = '.',
                            removeEntries: Boolean = true,
                            abbreviateName: Boolean = false): FormatBlock = apply { record =>
      val classAndMethodName = ClassAndMethodName.format(record)
      val lineNumber = if (record.lineNumber.nonEmpty) {
        p":${LineNumber.format(record)}"
      } else {
        ""
      }
      val v = Abbreviator(classAndMethodName, maxLength - lineNumber.length, separator, removeEntries, abbreviateName)
      p"$v$lineNumber"
    }
  }

  object LineNumber extends FormatBlock {
    override def format[M](record: LogRecord[M]): String = record.lineNumber.fold("")(_.toString)
  }

  object Message extends FormatBlock {
    override def format[M](record: LogRecord[M]): String = record.message
  }

  case class MDCReference(key: String) extends FormatBlock {
    override def format[M](record: LogRecord[M]): String = MDC.get(key).orNull
  }

  object MDCAll extends FormatBlock {
    override def format[M](record: LogRecord[M]): String = {
      val map = MDC.map
      if (map.nonEmpty) {
        MDC.map.map {
          case (key, value) => p"$key: $value"
        }.mkString(" (", ", ", ")")
      } else {
        ""
      }
    }
  }

  object NewLine extends FormatBlock {
    override def format[M](record: LogRecord[M]): String = System.lineSeparator
  }
}
