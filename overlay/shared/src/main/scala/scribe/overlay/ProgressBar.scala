package scribe.overlay

import scribe.ANSI

/**
 * Renders ANSI-styled progress bar strings.
 */
object ProgressBar {
  /**
   * Build an ANSI-styled progress bar string (no newline).
   *
   * Format: `[████……..]  42%  Optional label`
   *
   * @param width     inner bar width (number of cells between brackets)
   * @param percent   progress percentage, clamped to 0-100
   * @param label     optional label appended after the percentage
   * @param fillBg    background color for the filled portion
   * @param fillFg    foreground color for the filled portion
   * @return a single line without trailing newline
   */
  def render(width: Int = 40,
             percent: Int = 0,
             label: Option[String] = None,
             fillBg: ANSI = ANSI.bg.BrightGreen,
             fillFg: ANSI = ANSI.fg.Black): String = {
    val w = math.max(1, width)
    val p = math.max(0, math.min(100, percent))
    val filled = math.round(w * (p / 100.0)).toInt
    val empty = w - filled

    val emptyBg = ANSI.bg.Gray

    val sb = new StringBuilder(w + 64)

    if (filled > 0) {
      sb.append(fillBg(fillFg(" " * filled)))
    }
    if (empty > 0) {
      sb.append(emptyBg(" " * empty))
    }

    sb.append(" ")
    sb.append(ANSI.fx.Bold(f"$p%3d%%"))

    label.foreach { l =>
      sb.append(" - ")
      sb.append(l)
    }

    sb.append(ANSI.ctrl.Reset)
    sb.toString()
  }
}
