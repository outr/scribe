package scribe.format

import scribe._
import perfolation._
import scribe.output.{LogOutput, TextOutput}
import scribe.util.Abbreviator

trait FormatBlock {
  def format[M](record: LogRecord[M]): LogOutput

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

  def map(f: LogOutput => LogOutput): FormatBlock = FormatBlock.Mapped(this, f)

  def mapPlain(f: String => String): FormatBlock = FormatBlock.MappedPlain(this, f)

  def padRight(length: Int, padding: Char = ' '): FormatBlock = new RightPaddingBlock(this, length, padding)
}

object FormatBlock {
  def apply(f: LogRecord[_] => LogOutput): FormatBlock = new FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = f(record)
  }

  case class Mapped(block: FormatBlock, f: LogOutput => LogOutput) extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = f(block.format(record))
  }

  case class MappedPlain(block: FormatBlock, f: String => String) extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = block.format(record).map(f)
  }

  case class RawString(s: String) extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(s)
  }

  object TimeStamp extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(record.timeStamp.toString)
  }

  object Time extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(p"${record.timeStamp.t.T}")
  }

  object Date {
    object Standard extends FormatBlock {
      private lazy val cache = new ThreadLocal[String] {
        override def initialValue(): String = ""
      }
      private lazy val lastValue = new ThreadLocal[Long] {
        override def initialValue(): Long = 0L
      }

      override def format[M](record: LogRecord[M]): LogOutput = {
        val l = record.timeStamp
        if (l - lastValue.get() > 1000L) {
          val d = p"${l.t.Y}.${l.t.m}.${l.t.d} ${l.t.T}"
          cache.set(d)
          lastValue.set(l)
          new TextOutput(d)
        } else {
          new TextOutput(cache.get())
        }
      }
    }
    object Full extends FormatBlock {
      override def format[M](record: LogRecord[M]): LogOutput = {
        val l = record.timeStamp
        val d = p"${l.t.Y}.${l.t.m}.${l.t.d} ${l.t.T}:${l.t.L}"
        new TextOutput(d)
      }
    }
  }

  object ThreadName extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(record.thread.getName)
  }

  object Level extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(record.level.name)

    object PaddedRight extends FormatBlock {
      override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(record.level.namePaddedRight)
    }
  }

  object FileName extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(record.fileName)
  }

  object ClassName extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(record.className)
  }

  object MethodName extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(record.methodName.getOrElse(""))
  }

  object ClassAndMethodName extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = {
      val className = ClassName.format(record).plainText
      val methodName = if (record.methodName.nonEmpty) {
        p".${MethodName.format(record).plainText}"
      } else {
        ""
      }
      new TextOutput(p"$className$methodName")
    }
  }

  object Position extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = {
      val line = if (record.line.nonEmpty) {
        p":${LineNumber.format(record).plainText}"
      } else {
        ""
      }
      val column = if (record.column.nonEmpty) {
        p":${ColumnNumber.format(record).plainText}"
      } else {
        ""
      }
      new TextOutput(p"${ClassAndMethodName.format(record).plainText}$line$column")
    }

    override def abbreviate(maxLength: Int,
                            padded: Boolean = false,
                            separator: Char = '.',
                            removeEntries: Boolean = true,
                            abbreviateName: Boolean = false): FormatBlock = apply { record =>
      val classAndMethodName = ClassAndMethodName.format(record).plainText
      val line = if (record.line.nonEmpty) {
        p":${LineNumber.format(record).plainText}"
      } else {
        ""
      }
      val column = if (record.column.nonEmpty) {
        p":${ColumnNumber.format(record).plainText}"
      } else {
        ""
      }
      val v = Abbreviator(classAndMethodName, maxLength - line.length, separator, removeEntries, abbreviateName)
      new TextOutput(p"$v$line$column")
    }
  }

  object LineNumber extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(record.line.fold("")(_.toString))
  }

  object ColumnNumber extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(record.column.fold("")(_.toString))
  }

  object Message extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = record.message
  }

  case class MDCReference(key: String) extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(MDC.get(key).orNull)
  }

  object MDCAll extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = {
      val map = MDC.map
      if (map.nonEmpty) {
        new TextOutput(MDC.map.map {
          case (key, value) => p"$key: ${value()}"
        }.mkString(" (", ", ", ")"))
      } else {
        new TextOutput("")
      }
    }
  }

  object NewLine extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(System.lineSeparator)
  }
}
