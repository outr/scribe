package scribe

import scala.io.AnsiColor

object ANSI {
  private lazy val threadLocal = new ThreadLocal[Map[String, ANSI]] {
    override def initialValue(): Map[String, ANSI] = Map.empty
  }

  object bg {
    private def create(value: String): ANSI = ANSI(value, "bg", AnsiColor.RESET)

    lazy val Black: ANSI = create("[40m")
    lazy val Blue: ANSI = create("[44m")
    lazy val Cyan: ANSI = create("[46m")
    lazy val Green: ANSI = create("[42m")
    lazy val Magenta: ANSI = create("[45m")
    lazy val Red: ANSI = create("[41m")
    lazy val White: ANSI = create("[47m")
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
    def CursorBack(characters: Int = 1): String = s"""\\033[${characters}D"""
    def CursorDown(lines: Int = 1): String = s"\\033[${lines}B"
    def CursorForward(characters: Int = 1): String = s"""\\033[${characters}C"""
    def CursorUp(lines: Int = 1): String = s"""\\033[${lines}A"""
    def EraseLine: String = "\u001b[K"
    def FormFeed: String = "\f"
    def NewLine: String = "\n"
    def Reset: String = AnsiColor.RESET
    def RestorePosition: String = "[u"
    def Return: String = "\r"
    def SavePosition: String = "[s"
    def Tab: String = "\t"
  }

  object fg {
    private def create(value: String): ANSI = ANSI(value, "fg", AnsiColor.RESET)

    lazy val Black: ANSI = create("[30m")
    lazy val Blue: ANSI = create("[34m")
    lazy val Cyan: ANSI = create("[36m")
    lazy val Green: ANSI = create("[32m")
    lazy val Magenta: ANSI = create("[35m")
    lazy val Red: ANSI = create("[31m")
    lazy val White: ANSI = create("[37m")
    lazy val Yellow: ANSI = create("[33m")

    lazy val Gray: ANSI = create("[30;1m")
    lazy val BrightBlue: ANSI = create("[34;1m")
    lazy val BrightCyan: ANSI = create("[36;1m")
    lazy val BrightGreen: ANSI = create("[32;1m")
    lazy val BrightMagenta: ANSI = create("[35;1m")
    lazy val BrightRed: ANSI = create("[31;1m")
    lazy val BrightWhite: ANSI = create("[37;1m")
    lazy val BrightYellow: ANSI = create("[33;1m")
  }

  object fx {
    private def create(value: String): ANSI = ANSI(value, "fx", AnsiColor.RESET)

    lazy val Blink: ANSI = create("[5m")
    lazy val Bold: ANSI = create("[1m")
    lazy val Invisible: ANSI = create("[8m")
    lazy val Italic: ANSI = create("[3m")
    lazy val Reversed: ANSI = create("[7m")
    lazy val Strikethrough: ANSI = create("[9m")
    lazy val Underline: ANSI = create("[4m")
  }
}

case class ANSI(ansi: String, `type`: String, default: String) {
  def apply(value: => String): String = {
    val map = ANSI.threadLocal.get()
    val previous = map.get(`type`)
    ANSI.threadLocal.set(map + (`type` -> this))
    val reset = previous.map(_.ansi).getOrElse(default)
    val end = if (reset == AnsiColor.RESET) {
      s"$reset${map.filterNot(_._1 == `type`).map(_._2.ansi).mkString}"
    } else {
      reset
    }
    try {
      s"$ansi$value$end"
    } finally {
      ANSI.threadLocal.set(map)
    }
  }
}