package scribe.overlay

import scribe.Logger

import java.io.PrintStream
import java.nio.charset.StandardCharsets
import scala.util.Try

/**
 * Wraps the real terminal stream (Logger.system.out) and provides synchronized
 * write access plus terminal size detection. All physical terminal output in the
 * overlay system goes through this single point.
 */
class Terminal {
  // Write directly to /dev/tty, bypassing any stdout pipe (e.g. sbt fork).
  // This is how TUI apps like btop, vim, etc. talk to the terminal.
  val stream: PrintStream = scala.util.Try {
    new PrintStream(new java.io.FileOutputStream("/dev/tty"), true)
  }.getOrElse(Logger.system.out)

  private var cachedSize: (Int, Int) = querySize()
  private var lastSizeQuery: Long = System.currentTimeMillis()
  private val sizeCacheMs: Long = 1000L

  def size(): (Int, Int) = synchronized {
    val now = System.currentTimeMillis()
    if (now - lastSizeQuery > sizeCacheMs) {
      cachedSize = querySize()
      lastSizeQuery = now
    }
    cachedSize
  }

  def write(s: String): Unit = synchronized {
    stream.print(s)
    stream.flush()
  }

  private def querySize(): (Int, Int) = {
    Try {
      val pb = new ProcessBuilder("sh", "-c", "stty size < /dev/tty")
      val p = pb.redirectErrorStream(true).start()
      val out = new String(p.getInputStream.readAllBytes(), StandardCharsets.UTF_8).trim
      p.waitFor()
      val Array(r, c) = out.split("\\s+")
      (r.toInt, c.toInt)
    }.getOrElse(24 -> 80)
  }
}
