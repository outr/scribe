package scribe.format

import perfolation._
import scribe._
import scribe.data.MDC
import scribe.output.{CompositeOutput, EmptyOutput, LogOutput, TextOutput}
import scribe.util.Abbreviator

import scala.language.implicitConversions

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
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(s"${record.timeStamp.t.T}")
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
          val d = s"${l.t.Y}.${l.t.m}.${l.t.d} ${l.t.T}"
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
        val d = s"${l.t.Y}.${l.t.m}.${l.t.d} ${l.t.T}:${l.t.L}"
        new TextOutput(d)
      }
    }
    object Counter extends FormatBlock {
      private lazy val cache = new ThreadLocal[String] {
        override def initialValue(): String = ""
      }
      private lazy val lastValue = new ThreadLocal[Long] {
        override def initialValue(): Long = 0L
      }
      private lazy val counter = new ThreadLocal[Long] {
        override def initialValue(): Long = 0L
      }

      override def format[M](record: LogRecord[M]): LogOutput = {
        val l = record.timeStamp
        if (l - lastValue.get() > 1000L) {
          counter.set(0L)
          val d = s"${l.t.Y}.${l.t.m}.${l.t.d} ${l.t.T}:000"
          cache.set(d)
          lastValue.set(l)
          new TextOutput(d)
        } else {
          val c = counter.get() + 1
          counter.set(c)
          val cnt = c match {
            case _ if c >= 100 => c.toString
            case _ if c >= 10 => s"0$c"
            case _ => s"00$c"
          }
          new TextOutput(s"${cache.get()}:$cnt")
        }
      }
    }
  }

  object ThreadName extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(record.thread.getName)
  }

  object Level extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(record.level.name)

    object PaddedRight extends FormatBlock {
      override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(record.level.namePadded)
    }
  }

  object FileName extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(record.fileName)
  }

  object ClassName extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(record.className)
  }

  object ClassNameSimple extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = {
      val cn = record.className
      val index = cn.lastIndexOf('.')
      val simple = if (index != -1) {
        cn.substring(index + 1)
      } else {
        cn
      }
      new TextOutput(simple)
    }
  }

  object MethodName extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(record.methodName.getOrElse(""))
  }

  object ClassAndMethodName extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = {
      val className = ClassName.format(record).plainText
      val methodName = if (record.methodName.nonEmpty) {
        s".${MethodName.format(record).plainText}"
      } else {
        ""
      }
      new TextOutput(s"$className$methodName")
    }
  }

  object ClassAndMethodNameSimple extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = {
      val className = ClassNameSimple.format(record).plainText
      val methodName = if (record.methodName.nonEmpty) {
        s".${MethodName.format(record).plainText}"
      } else {
        ""
      }
      new TextOutput(s"$className$methodName")
    }
  }

  object Position extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = {
      val line = if (record.line.nonEmpty) {
        s":${LineNumber.format(record).plainText}"
      } else {
        ""
      }
      val column = if (record.column.nonEmpty) {
        s":${ColumnNumber.format(record).plainText}"
      } else {
        ""
      }
      new TextOutput(s"${ClassAndMethodName.format(record).plainText}$line$column")
    }

    override def abbreviate(maxLength: Int,
                            padded: Boolean = false,
                            separator: Char = '.',
                            removeEntries: Boolean = false,
                            abbreviateName: Boolean = false): FormatBlock = apply { record =>
      val line = if (record.line.nonEmpty) {
        s":${LineNumber.format(record).plainText}"
      } else {
        ""
      }
      val column = if (record.column.nonEmpty) {
        s":${ColumnNumber.format(record).plainText}"
      } else {
        ""
      }
      val className = ClassName.format(record).plainText
      val methodName = MethodName.format(record).plainText
      val cna = Abbreviator(className, maxLength - line.length, separator, removeEntries, abbreviateName)
      new TextOutput(s"$cna.$methodName$line$column")
    }
  }

  object PositionSimple extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = {
      val line = if (record.line.nonEmpty) {
        s":${LineNumber.format(record).plainText}"
      } else {
        ""
      }
      val column = if (record.column.nonEmpty) {
        s":${ColumnNumber.format(record).plainText}"
      } else {
        ""
      }
      new TextOutput(s"${ClassAndMethodNameSimple.format(record).plainText}$line$column")
    }
  }

  object LineNumber extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(record.line.fold("")(_.toString))
  }

  object ColumnNumber extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(record.column.fold("")(_.toString))
  }

  object Message extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = record.logOutput
  }

  case class MDCReference(key: String,
                          default: () => Any,
                          prefix: Option[FormatBlock],
                          postfix: Option[FormatBlock]) extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = MDC.get(key) match {
      case Some(value) => {
        val text = new TextOutput(value.toString)
        if (prefix.nonEmpty || postfix.nonEmpty) {
          new CompositeOutput(List(prefix.map(_.format(record)), Option(text), postfix.map(_.format(record))).flatten)
        } else {
          text
        }
      }
      case None => new TextOutput(default().toString)
    }
  }

  object MDCAll extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = {
      val map = MDC.map ++ record.data
      if (map.nonEmpty) {
        new TextOutput(MDC.map.map {
          case (key, value) => s"$key: ${value()}"
        }.mkString(" (", ", ", ")"))
      } else {
        new TextOutput("")
      }
    }
  }

  object NewLine extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(System.lineSeparator)
  }

  case class MultiLine(maxChars: Int = MultiLine.DefaultMaxChars, prefix: String = "    ", blocks: List[FormatBlock]) extends FormatBlock {
    override def format[M](record: LogRecord[M]): LogOutput = {
      val pre = new TextOutput(prefix)
      val max = maxChars - prefix.length
      val newLine = NewLine.format(record)
      val outputs = MultiLine.splitNewLines(blocks.map(_.format(record)))
      val list = outputs.flatMap { output =>
        var current = output
        var list = List.empty[LogOutput]
        while (current.length > max) {
          val (left, right) = current.splitAt(max)
          list = list ::: List(pre, left, newLine)
          current = right
        }
        list = list ::: List(pre, current)
        list
      }.dropRight(1)
      new CompositeOutput(list)
    }
  }

  object MultiLine {
    val DefaultMaxChars: Int = 120

    def splitNewLines(outputs: List[LogOutput]): List[LogOutput] = outputs.flatMap { output =>
      var lo = output
      var plainText = output.plainText
      var splitted = List.empty[LogOutput]
      def process(): Unit = {
        val index = plainText.indexOf('\n')
        if (index == -1) {
          splitted = lo :: splitted
          // Finished
        } else {
          val (one, two) = lo.splitAt(index + 1)
          splitted = one :: splitted
          lo = two
          plainText = plainText.substring(index + 1)
          process()
        }
      }
      process()
      splitted.reverse
    }
  }
}