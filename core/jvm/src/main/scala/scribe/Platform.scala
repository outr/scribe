package scribe

import moduload.Moduload
import scribe.output.format.{ANSIOutputFormat, ASCIIOutputFormat, OutputFormat}
import scribe.writer.{SystemWriter, Writer}

import java.io.{BufferedReader, InputStreamReader}
import scala.concurrent.ExecutionContext
import scala.util.Try

object Platform extends PlatformImplementation {
  private val maximumColumns: Int = 5000
  private var lastChecked: Long = 0L
  private var cachedColumns: Int = -1
  private var cachedRows: Int = -1

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
    updateConsoleSize()
    if (cachedColumns == -1) {
      maximumColumns
    } else {
      cachedColumns + columnsAdjust
    }
  }


  override def rows: Int = {
    updateConsoleSize()
    cachedRows
  }

  private def updateConsoleSize(): Unit = {
    val now = System.currentTimeMillis()
    if (now - lastChecked >= columnCheckFrequency) {
      lastChecked = now
      val (c, r) = queryTput()
      cachedColumns = c
      cachedRows = r
    }
  }

  def queryTput(): (Int, Int) = Try {
    val pb = new ProcessBuilder("bash", "-c", "tput cols lines 2> /dev/tty")
    val p = pb.start()
    val i = new BufferedReader(new InputStreamReader(p.getInputStream))
    try {
      val columns = i.readLine().trim.toInt
      val rows = i.readLine().trim.toInt
      (columns, rows)
    } finally {
      i.close()
    }
  }.getOrElse((-1, -1))

  override def executionContext: ExecutionContext = ExecutionContext.global
}