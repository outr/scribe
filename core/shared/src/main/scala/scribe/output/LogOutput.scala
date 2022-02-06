package scribe.output

sealed trait LogOutput extends Any {
  def plainText: String
  def length: Int = plainText.length

  def map(f: String => String): LogOutput
  def splitAt(index: Int): (LogOutput, LogOutput)
}

object LogOutput {
  lazy val NewLine: LogOutput = new TextOutput("\n")
}

object EmptyOutput extends LogOutput {
  override val plainText: String = ""

  override def map(f: String => String): LogOutput = f(plainText) match {
    case "" => EmptyOutput
    case s => new TextOutput(s)
  }

  override def splitAt(index: Int): (LogOutput, LogOutput) = (EmptyOutput, EmptyOutput)
}

class TextOutput(val plainText: String) extends AnyVal with LogOutput {
  override def map(f: String => String): LogOutput = new TextOutput(f(plainText))

  override def splitAt(index: Int): (LogOutput, LogOutput) =
    (new TextOutput(plainText.substring(0, index)), new TextOutput(plainText.substring(index)))
}

class CompositeOutput(val entries: List[LogOutput]) extends LogOutput {
  override lazy val plainText: String = entries.map(_.plainText).mkString

  override def map(f: String => String): LogOutput = new CompositeOutput(entries.map(_.map(f)))

  override def splitAt(index: Int): (LogOutput, LogOutput) = {
    def recurse(left: List[LogOutput], right: List[LogOutput], chars: Int): (LogOutput, LogOutput) = {
      if (right.isEmpty) {
        (new CompositeOutput(left), EmptyOutput)
      } else {
        val head = right.head
        val length = head.length
        chars + length match {
          case l if l == index => (new CompositeOutput(left ::: List(head)), new CompositeOutput(right.tail))
          case l if l > index =>
            val (left1, left2) = head.splitAt(index - chars)
            (new CompositeOutput(left ::: List(left1)), new CompositeOutput(left2 :: right.tail))
          case l => recurse(left ::: List(head), right.tail, l)
        }
      }
    }
    recurse(Nil, entries, 0)
  }
}

class ColoredOutput(val color: Color, val output: LogOutput) extends LogOutput {
  override lazy val plainText: String = output.plainText

  override def map(f: String => String): LogOutput = new ColoredOutput(color, output.map(f))

  override def splitAt(index: Int): (LogOutput, LogOutput) = {
    val (left, right) = output.splitAt(index)
    (new ColoredOutput(color, left), new ColoredOutput(color, right))
  }
}

class BackgroundColoredOutput(val color: Color, val output: LogOutput) extends LogOutput {
  override lazy val plainText: String = output.plainText

  override def map(f: String => String): LogOutput = new BackgroundColoredOutput(color, output.map(f))

  override def splitAt(index: Int): (LogOutput, LogOutput) = {
    val (left, right) = output.splitAt(index)
    (new BackgroundColoredOutput(color, left), new BackgroundColoredOutput(color, right))
  }
}

class URLOutput(val url: String, val output: LogOutput) extends LogOutput {
  override def plainText: String = output.plainText

  override def map(f: String => String): LogOutput = new URLOutput(url, output.map(f))

  override def splitAt(index: Int): (LogOutput, LogOutput) = {
    val (left, right) = output.splitAt(index)
    (new URLOutput(url, left), new URLOutput(url, right))
  }
}

class BoldOutput(val output: LogOutput) extends AnyVal with LogOutput {
  override def plainText: String = output.plainText

  override def map(f: String => String): LogOutput = new BoldOutput(output.map(f))

  override def splitAt(index: Int): (LogOutput, LogOutput) = {
    val (left, right) = output.splitAt(index)
    (new BoldOutput(left), new BoldOutput(right))
  }
}

class ItalicOutput(val output: LogOutput) extends AnyVal with LogOutput {
  override def plainText: String = output.plainText

  override def map(f: String => String): LogOutput = new ItalicOutput(output.map(f))

  override def splitAt(index: Int): (LogOutput, LogOutput) = {
    val (left, right) = output.splitAt(index)
    (new ItalicOutput(left), new ItalicOutput(right))
  }
}

class UnderlineOutput(val output: LogOutput) extends AnyVal with LogOutput {
  override def plainText: String = output.plainText

  override def map(f: String => String): LogOutput = new UnderlineOutput(output.map(f))

  override def splitAt(index: Int): (LogOutput, LogOutput) = {
    val (left, right) = output.splitAt(index)
    (new UnderlineOutput(left), new UnderlineOutput(right))
  }
}

class StrikethroughOutput(val output: LogOutput) extends AnyVal with LogOutput {
  override def plainText: String = output.plainText

  override def map(f: String => String): LogOutput = new StrikethroughOutput(output.map(f))

  override def splitAt(index: Int): (LogOutput, LogOutput) = {
    val (left, right) = output.splitAt(index)
    (new StrikethroughOutput(left), new StrikethroughOutput(right))
  }
}

sealed trait Color {
  lazy val name: String = getClass.getSimpleName.replace("$", "").toLowerCase
}

object Color {
  case object Black extends Color
  case object Blue extends Color
  case object Cyan extends Color
  case object Green extends Color
  case object Magenta extends Color
  case object Red extends Color
  case object White extends Color
  case object Yellow extends Color

  case object Gray extends Color
  case object BrightBlue extends Color
  case object BrightCyan extends Color
  case object BrightGreen extends Color
  case object BrightMagenta extends Color
  case object BrightRed extends Color
  case object BrightWhite extends Color
  case object BrightYellow extends Color
}