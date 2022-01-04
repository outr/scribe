package scribe

import moduload.Moduload
import org.jline.terminal.TerminalBuilder
import scribe.format.FormatBlock.MultiLine
import scribe.output.format.{ANSIOutputFormat, ASCIIOutputFormat, OutputFormat}
import scribe.writer.{SystemOutputWriter, Writer}

object Platform extends PlatformImplementation {
  private lazy val terminal = TerminalBuilder.terminal()
  private var lastChecked: Long = 0L
  private var cachedColumns: Int = MultiLine.DefaultMaxChars

  var columnCheckFrequency: Long = 5 * 1000L

  def isJVM: Boolean = true
  def isJS: Boolean = false
  def isNative: Boolean = false

  def init(): Unit = {
    // Load Moduload
    Moduload.load()
  }

  lazy val supportsANSI: Boolean = sys.env.contains("TERM")

  def outputFormat(): OutputFormat = sys.env.get("SCRIBE_OUTPUT_FORMAT").map(_.toUpperCase) match {
    case Some("ANSI") => ANSIOutputFormat
    case Some("ASCII") => ASCIIOutputFormat
    case None if supportsANSI => ANSIOutputFormat
    case None => ASCIIOutputFormat
    case f =>
      System.err.println(s"Unexpected output format specified in SCRIBE_OUTPUT_FORMAT: $f, using ASCII")
      ASCIIOutputFormat
  }

  override def consoleWriter: Writer = SystemOutputWriter

  override def columns: Int = {
    val now = System.currentTimeMillis()
    if (now - lastChecked >= columnCheckFrequency) {
      lastChecked = now
      cachedColumns = terminal.getSize.getColumns match {
        case 0 => MultiLine.DefaultMaxChars
        case n => n
      }
    }
    cachedColumns
  }
}