package scribe.overlay

/**
 * Vertical alignment for a banner on the terminal screen.
 */
sealed trait VerticalPosition
object VerticalPosition {
  case object Top extends VerticalPosition
  case object Center extends VerticalPosition
  case object Bottom extends VerticalPosition
}

/**
 * Horizontal alignment for a banner on the terminal screen.
 */
sealed trait HorizontalPosition
object HorizontalPosition {
  case object Left extends HorizontalPosition
  case object Center extends HorizontalPosition
  case object Right extends HorizontalPosition
}

/**
 * Screen position for a floating banner overlay.
 *
 * @param vertical   vertical alignment anchor
 * @param horizontal horizontal alignment anchor
 * @param rowOffset  row offset from the anchor position (positive = down, negative = up)
 * @param colOffset  column offset from the anchor position (positive = right, negative = left)
 */
case class BannerPosition(vertical: VerticalPosition = VerticalPosition.Center,
                          horizontal: HorizontalPosition = HorizontalPosition.Center,
                          rowOffset: Int = 0,
                          colOffset: Int = 0)

object BannerPosition {
  val TopLeft: BannerPosition = BannerPosition(VerticalPosition.Top, HorizontalPosition.Left)
  val TopCenter: BannerPosition = BannerPosition(VerticalPosition.Top, HorizontalPosition.Center)
  val TopRight: BannerPosition = BannerPosition(VerticalPosition.Top, HorizontalPosition.Right)
  val CenterLeft: BannerPosition = BannerPosition(VerticalPosition.Center, HorizontalPosition.Left)
  val Center: BannerPosition = BannerPosition(VerticalPosition.Center, HorizontalPosition.Center)
  val CenterRight: BannerPosition = BannerPosition(VerticalPosition.Center, HorizontalPosition.Right)
  val BottomLeft: BannerPosition = BannerPosition(VerticalPosition.Bottom, HorizontalPosition.Left)
  val BottomCenter: BannerPosition = BannerPosition(VerticalPosition.Bottom, HorizontalPosition.Center)
  val BottomRight: BannerPosition = BannerPosition(VerticalPosition.Bottom, HorizontalPosition.Right)
}
