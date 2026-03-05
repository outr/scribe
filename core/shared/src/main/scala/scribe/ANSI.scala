package scribe

import scala.io.AnsiColor

object ANSI {
  private lazy val threadLocal = new ThreadLocal[Map[String, ANSI]] {
    override def initialValue(): Map[String, ANSI] = Map.empty
  }

  private val escCode: Char = 0x1B.toChar

  object bg {
    private def create(value: String): ANSI = ANSI(s"\u001b$value", "bg", AnsiColor.RESET)

    lazy val Black: ANSI = create("[40m")
    lazy val Blue: ANSI = create("[44m")
    lazy val Cyan: ANSI = create("[46m")
    lazy val Green: ANSI = create("[42m")
    lazy val Magenta: ANSI = create("[45m")
    lazy val Red: ANSI = create("[41m")
    lazy val White: ANSI = create("[47m")
    lazy val Yellow: ANSI = create("[43m")

    lazy val Gray: ANSI = create("[40;1m")
    lazy val BrightBlue: ANSI = create("[44;1m")
    lazy val BrightCyan: ANSI = create("[46;1m")
    lazy val BrightGreen: ANSI = create("[42;1m")
    lazy val BrightMagenta: ANSI = create("[45;1m")
    lazy val BrightRed: ANSI = create("[41;1m")
    lazy val BrightWhite: ANSI = create("[47;1m")
    lazy val BrightYellow: ANSI = create("[43;1m")
  }

  private def esc(suffix: String): String = s"$escCode[$suffix"
  private def esc(num: Int, suffix: String): String = esc(s"$num$suffix")
  private def esc(num1: Int, num2: Int, suffix: String): String = esc(s"$num1;$num2$suffix")

  object ctrl {
    /** Repeat a literal string `s`, `count` times. */
    private def create(count: Int, s: String): String = s * count

    /** Backspace (BS): move cursor left by `characters` without erasing text.
     * Equivalent to sending '\b' repeatedly. Some terminals won't move past column 1.
     * @param characters number of columns to move left
     */
    def Backspace(characters: Int = 1): String = create(characters, "\b")

    /** Erase in Display (ED 2): clear the entire screen buffer.
     * Most terminals leave the cursor position unchanged; some also clear scrollback.
     */
    val ClearScreen: String = esc(2, "J")

    /** Cursor Backward (CUB): move cursor left by `characters` columns.
     * Does not erase characters.
     * @param characters columns to move left (>= 0)
     */
    def CursorBack(characters: Int = 1): String = esc(characters, "D")

    /** Cursor Down (CUD): move cursor down by `lines` lines, same column.
     * @param lines lines to move down (>= 0)
     */
    def CursorDown(lines: Int = 1): String = esc(lines, "B")

    /** Cursor Forward (CUF): move cursor right by `characters` columns.
     * Does not wrap by default unless the terminal is in wrap mode.
     * @param characters columns to move right (>= 0)
     */
    def CursorForward(characters: Int = 1): String = esc(characters, "C")

    /** Cursor Up (CUU): move cursor up by `rows` lines, same column.
     * @param rows lines to move up (>= 0)
     */
    def CursorUp(rows: Int = 1): String = esc(rows, "A")

    /** Cursor Position (CUP): move cursor to absolute 1-based row/column.
     * Columns/rows less than 1 are clamped by most terminals.
     * @param row 1-based target row
     * @param column 1-based target column
     */
    def CursorMove(row: Int, column: Int): String = esc(row, column, "H")

    /** Erase in Line (EL 0): erase from cursor to end of line, inclusive. */
    val EraseLineEnd: String = esc("K")

    /** Erase in Line (EL 1): erase from start of line to cursor, inclusive. */
    val EraseLineStart: String = esc(1, "K")

    /** Erase in Line (EL 2): erase the entire current line. Cursor column is unchanged. */
    val EraseLineAll: String = esc(2, "K")

    /** Cursor Next Line (CNL): move to beginning (column 1) of the next `lines` lines.
     * @param lines number of lines to advance (>= 0)
     */
    def NextLine(lines: Int = 1): String = esc(lines, "E")

    /** Cursor Previous Line (CPL): move to beginning (column 1) of the previous `lines` lines.
     * @param lines number of lines to move back (>= 0)
     */
    def PrevLine(lines: Int = 1): String = esc(lines, "F")

    /** Set Top/Bottom Margins (DECSTBM): define a scrolling region from `top` to `bottom`.
     * Lines within the region scroll on line feeds; lines outside do not.
     * Useful for reserving a fixed status line at the bottom.
     * @param top    1-based top row of the scroll region
     * @param bottom 1-based bottom row (must be >= top)
     */
    def SetScrollRegion(top: Int, bottom: Int): String = esc(s"$top;${bottom}r")

    /** Reset Scrolling Region (DECSTBM reset): restore full-screen scrolling. */
    val ResetScrollRegion: String = esc("r")

    /** DECTCEM: hide the text cursor. */
    val HideCursor: String = esc("?25l")

    /** DECTCEM: show the text cursor. */
    val ShowCursor: String = esc("?25h")

    /** Form Feed (FF): rarely used; may clear or advance page depending on terminal. */
    val FormFeed: String = "\f"

    /** Line Feed (LF): move down one line; column typically unchanged (unless LNM is set). */
    val NewLine: String = "\n"

    /** Select Graphic Rendition (SGR 0): reset colors/effects to defaults. */
    val Reset: String = AnsiColor.RESET

    /** Restore Cursor (DECRC / CSI u): restore position saved by [[SavePosition]]. */
    val RestorePosition: String = esc("u")

    /** Carriage Return (CR): move to column 1 on the current line (no line feed). */
    val Return: String = "\r"

    /** Save Cursor (DECSC / CSI s): save current cursor position for later restore. */
    val SavePosition: String = esc("s")

    /** Horizontal Tab (HT): move to next tab stop (commonly every 8 columns). */
    val Tab: String = "\t"

    /** Index (IND, ESC D): move cursor down one line within the scroll region.
     * At the bottom margin, scrolls the region up by one line. Column is unchanged.
     */
    val Index: String = s"${escCode}D"

    /** Reverse Index (RI, ESC M): move cursor up one line within the scroll region.
     * At the top margin, scrolls the region down by one line. Column is unchanged.
     */
    val ReverseIndex: String = s"${escCode}M"

    /** Scroll Up (SU): scroll the active scroll region up by `lines` lines.
     * Creates blank lines at the bottom of the region.
     */
    def ScrollUp(lines: Int = 1): String = esc(lines, "S")

    /** Scroll Down (SD): scroll the active scroll region down by `lines` lines.
     * Creates blank lines at the top of the region.
     */
    def ScrollDown(lines: Int = 1): String = esc(lines, "T")
  }

  object fg {
    private def create(value: String): ANSI = ANSI(s"\u001b$value", "fg", AnsiColor.RESET)

    lazy val Black: ANSI = create("[30m")
    lazy val Blue: ANSI = create("[34m")
    lazy val Cyan: ANSI = create("[36m")
    lazy val Green: ANSI = create("[32m")
    lazy val Magenta: ANSI = create("[35m")
    lazy val Red: ANSI = create("[31m")
    lazy val White: ANSI = create("[37m")
    lazy val Yellow: ANSI = create("[33m")

    lazy val Gray: ANSI = create("[30;1m")
    lazy val BrightBlue: ANSI = create("[34;1m")
    lazy val BrightCyan: ANSI = create("[36;1m")
    lazy val BrightGreen: ANSI = create("[32;1m")
    lazy val BrightMagenta: ANSI = create("[35;1m")
    lazy val BrightRed: ANSI = create("[31;1m")
    lazy val BrightWhite: ANSI = create("[37;1m")
    lazy val BrightYellow: ANSI = create("[33;1m")
  }

  object fx {
    private def create(value: String): ANSI = ANSI(s"\u001b$value", "fx", AnsiColor.RESET)

    lazy val Blink: ANSI = create("[5m")
    lazy val Bold: ANSI = create("[1m")
    lazy val Invisible: ANSI = create("[8m")
    lazy val Italic: ANSI = create("[3m")
    lazy val Reversed: ANSI = create("[7m")
    lazy val Strikethrough: ANSI = create("[9m")
    lazy val Underline: ANSI = create("[4m")
  }
}

case class ANSI(ansi: String, category: String, default: String) {
  def apply(value: => String): String = {
    val map = ANSI.threadLocal.get()
    val previous = map.get(category)
    ANSI.threadLocal.set(map + (category -> this))
    val reset = previous.map(_.ansi).getOrElse(default)
    val end = if (reset == AnsiColor.RESET) {
      s"$reset${map.filterNot(_._1 == category).map(_._2.ansi).mkString}"
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
