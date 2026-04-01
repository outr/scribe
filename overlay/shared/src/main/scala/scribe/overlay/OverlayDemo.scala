package scribe.overlay

import scribe.ANSI

import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Demo application showing the overlay system in action.
 *
 * Run with: sbt 'overlayJVM/runMain scribe.overlay.OverlayDemo'
 */
object OverlayDemo extends App {
  val overlay = Overlay.create()

  // Progress bars for simulated downloads
  val download1 = overlay.progressBar("file1.zip", width = 30)
  val download2 = overlay.progressBar("file2.tar.gz", width = 30)
  val download3 = overlay.progressBar("file3.dat", width = 30)

  val downloads = List(
    (download1, "file1.zip", 3),
    (download2, "file2.tar.gz", 2),
    (download3, "file3.dat", 5)
  )

  val clockFmt = DateTimeFormatter.ofPattern("HH:mm:ss")

  def updateBanner(): Unit = {
    val time = LocalTime.now().format(clockFmt)
    val active = downloads.count(!_._1.isCompleted)
    val line1 = s"${ANSI.fx.Bold(ANSI.fg.Cyan(s"⏱  $time"))}"
    val line2 = s"${ANSI.fg.White(s"$active download(s) active")}"
    overlay.banner(s"$line1\n$line2")
  }

  var tick = 0
  val maxTicks = 100

  while (downloads.exists(!_._1.isCompleted) && tick <= maxTicks) {
    updateBanner()

    downloads.foreach { case (entry, name, speed) =>
      if (!entry.isCompleted) {
        val percent = math.min(100, tick * speed)
        entry.update(percent, name)
        if (percent >= 100) {
          entry.complete(s"Downloaded $name")
        }
      }
    }

    if (tick % 5 == 0) {
      scribe.info(s"Still working... tick $tick")
    }

    Thread.sleep(100)
    tick += 1
  }

  overlay.clearBanner()
  scribe.info("All downloads complete!")
  overlay.dispose()
}
