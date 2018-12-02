package scribe

import scala.io.AnsiColor
import perfolation._

object ANSI {
  private lazy val threadLocal = new ThreadLocal[Map[String, ANSI]] {
    override def initialValue(): Map[String, ANSI] = Map.empty
  }

  object bg {
    private def create(value: String): ANSI = ANSI(value, "bg", AnsiColor.RESET)

    lazy val Black: ANSI = create("\u001b[40m")
    lazy val Blue: ANSI = create("\u001b[44m")
    lazy val Cyan: ANSI = create("\u001b[46m")
    lazy val Green: ANSI = create("\u001b[42m")
    lazy val Magenta: ANSI = create("\u001b[45m")
    lazy val Red: ANSI = create("\u001b[41m")
    lazy val White: ANSI = create("\u001b[47m")
    lazy val Yellow: ANSI = create("\u001b[43m")

    lazy val Gray: ANSI = create("\u001b[40;1m")
    lazy val BrightBlue: ANSI = create("\u001b[44;1m")
    lazy val BrightCyan: ANSI = create("\u001b[46;1m")
    lazy val BrightGreen: ANSI = create("\u001b[42;1m")
    lazy val BrightMagenta: ANSI = create("\u001b[45;1m")
    lazy val BrightRed: ANSI = create("\u001b[41;1m")
    lazy val BrightWhite: ANSI = create("\u001b[47;1m")
    lazy val BrightYellow: ANSI = create("\u001b[43;1m")
  }

  object ctrl {
    private def create(count: Int, s: String): String = (0 until count).map(_ => s).mkString

    def Backspace(characters: Int = 1): String = create(characters, "\b")
    def ClearScreen: String = "\u001b[2J"
    def CursorBack(characters: Int = 1): String = p"\033[${characters}D"
    def CursorDown(lines: Int = 1): String = p"\033[${lines}B"
    def CursorForward(characters: Int = 1): String = p"\033[${characters}C"
    def CursorUp(lines: Int = 1): String = p"\033[${lines}A"
    def EraseLine: String = "\u001b[K"
    def FormFeed: String = "\f"
    def NewLine: String = "\n"
    def Reset: String = AnsiColor.RESET
    def RestorePosition: String = "\u001b[u"
    def Return: String = "\r"
    def SavePosition: String = "\u001b[s"
    def Tab: String = "\t"
  }

  object fg {
    private def create(value: String): ANSI = ANSI(value, "fg", AnsiColor.RESET)

    lazy val Black: ANSI = create("\u001b[30m")
    lazy val Blue: ANSI = create("\u001b[34m")
    lazy val Cyan: ANSI = create("\u001b[36m")
    lazy val Green: ANSI = create("\u001b[32m")
    lazy val Magenta: ANSI = create("\u001b[35m")
    lazy val Red: ANSI = create("\u001b[31m")
    lazy val White: ANSI = create("\u001b[37m")
    lazy val Yellow: ANSI = create("\u001b[33m")

    lazy val Gray: ANSI = create("\u001b[30;1m")
    lazy val BrightBlue: ANSI = create("\u001b[34;1m")
    lazy val BrightCyan: ANSI = create("\u001b[36;1m")
    lazy val BrightGreen: ANSI = create("\u001b[32;1m")
    lazy val BrightMagenta: ANSI = create("\u001b[35;1m")
    lazy val BrightRed: ANSI = create("\u001b[31;1m")
    lazy val BrightWhite: ANSI = create("\u001b[37;1m")
    lazy val BrightYellow: ANSI = create("\u001b[33;1m")
  }

  object fx {
    private def create(value: String): ANSI = ANSI(value, "fx", AnsiColor.RESET)

    lazy val Blink: ANSI = create("\u001b[5m")
    lazy val Bold: ANSI = create("\u001b[1m")
    lazy val Invisible: ANSI = create("\u001b[8m")
    lazy val Italic: ANSI = create("\u001b[3m")
    lazy val Reversed: ANSI = create("\u001b[7m")
    lazy val Strikethrough: ANSI = create("\u001b[9m")
    lazy val Underline: ANSI = create("\u001b[4m")
  }
}

case class ANSI(ansi: String, `type`: String, default: String) {
  def apply(value: => String): String = {
    val map = ANSI.threadLocal.get()
    val previous = map.get(`type`)
    ANSI.threadLocal.set(map + (`type` -> this))
    val reset = previous.map(_.ansi).getOrElse(default)
    val end = if (reset == AnsiColor.RESET) {
      p"$reset${map.filterNot(_._1 == `type`).map(_._2.ansi).mkString}"
    } else {
      reset
    }
    try {
      p"$ansi$value$end"
    } finally {
      ANSI.threadLocal.set(map)
    }
  }
}