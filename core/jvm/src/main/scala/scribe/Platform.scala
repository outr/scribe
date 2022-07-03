package scribe

import moduload.Moduload
import org.jline.terminal.TerminalBuilder
import scribe.output.format.{ANSIOutputFormat, ASCIIOutputFormat, OutputFormat}
import scribe.writer.{SystemWriter, Writer}

import java.io.{BufferedReader, InputStreamReader}
import scala.concurrent.ExecutionContext
import scala.util.Try

object Platform extends PlatformImplementation {
  private val maximumColumns: Int = 5000
  private lazy val terminal = TerminalBuilder.terminal()
  private var lastChecked: Long = 0L
  private var cachedColumns: Int = -1

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

  override def consoleWriter: Writer = SystemWriter

  override def columns: Int = {
    val now = System.currentTimeMillis()
    if (now - lastChecked >= columnCheckFrequency) {
      lastChecked = now
      cachedColumns = terminal.getSize.getColumns match {
        case 0 => tputColumns()
        case n => n
      }
    }
    if (cachedColumns == -1) {
      maximumColumns
    } else {
      cachedColumns + columnsAdjust
    }
  }

  def tputColumns(): Int = Try {
    val pb = new ProcessBuilder("bash", "-c", "tput cols 2> /dev/tty")
    val p = pb.start()
    val i = new BufferedReader(new InputStreamReader(p.getInputStream))
    try {
      val line = i.readLine()
      val columns = line.trim.toInt
      columns
    } finally {
      i.close()
    }
  }.getOrElse(-1)

  override def executionContext: ExecutionContext = ExecutionContext.global
}