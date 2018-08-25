package scribe

import scala.io.AnsiColor
import perfolation._

object ANSI {
  private lazy val threadLocal = new ThreadLocal[Map[String, ANSI]] {
    override def initialValue(): Map[String, ANSI] = Map.empty
  }

  object bg {
    private def create(value: String): ANSI = ANSI(value, "bg", AnsiColor.RESET)

    lazy val Black: ANSI = create(AnsiColor.BLACK_B)
    lazy val Blue: ANSI = create(AnsiColor.BLUE_B)
    lazy val Cyan: ANSI = create(AnsiColor.CYAN_B)
    lazy val Green: ANSI = create(AnsiColor.GREEN_B)
    lazy val Magenta: ANSI = create(AnsiColor.MAGENTA_B)
    lazy val Red: ANSI = create(AnsiColor.RED_B)
    lazy val White: ANSI = create(AnsiColor.WHITE_B)
    lazy val Yellow: ANSI = create(AnsiColor.YELLOW_B)
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

    lazy val Black: ANSI = create(AnsiColor.BLACK)
    lazy val Blue: ANSI = create(AnsiColor.BLUE)
    lazy val Cyan: ANSI = create(AnsiColor.CYAN)
    lazy val Green: ANSI = create(AnsiColor.GREEN)
    lazy val Magenta: ANSI = create(AnsiColor.MAGENTA)
    lazy val Red: ANSI = create(AnsiColor.RED)
    lazy val White: ANSI = create(AnsiColor.WHITE)
    lazy val Yellow: ANSI = create(AnsiColor.YELLOW)
  }

  object fx {
    private def create(value: String): ANSI = ANSI(value, "fx", AnsiColor.RESET)

    lazy val Blink: ANSI = create(AnsiColor.BLINK)
    lazy val Bold: ANSI = create(AnsiColor.BOLD)
    lazy val Invisible: ANSI = create(AnsiColor.INVISIBLE)
    lazy val Reversed: ANSI = create(AnsiColor.REVERSED)
    lazy val Underlined: ANSI = create(AnsiColor.UNDERLINED)
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