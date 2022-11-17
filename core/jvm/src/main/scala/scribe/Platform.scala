package scribe

import moduload.Moduload
import scribe.output.format.{ANSIOutputFormat, ASCIIOutputFormat, OutputFormat}
import scribe.writer.{SystemWriter, Writer}

import java.io.{BufferedReader, File, InputStreamReader}
import java.nio.file.Files
import scala.concurrent.ExecutionContext
import scala.util.Try

object Platform extends PlatformImplementation {
  var maximumColumns: Int = 5000
  var minimumColumns: Int = 10
  var minimumRows: Int = 5
  var columnsOverride: Option[Int] = None
  var rowsOverride: Option[Int] = None

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

  override def columns: Int = columnsOverride.getOrElse {
    updateConsoleSize()
    if (cachedColumns < minimumColumns) {
      maximumColumns
    } else {
      cachedColumns + columnsAdjust
    }
  }


  override def rows: Int = rowsOverride.getOrElse {
    updateConsoleSize()
    if (cachedRows < minimumRows) {
      -1
    } else {
      cachedRows
    }
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

  private lazy val scriptFile = new File(System.getProperty("user.home"), ".cursor-position.sh")

  def cursor(): (Int, Int) = Try {
    if (!scriptFile.isFile) {
      val script =
        """#!/bin/bash
          |exec < /dev/tty
          |oldstty=$(stty -g)
          |stty raw -echo min 0
          |echo -en "\033[6n" > /dev/tty
          |IFS=';' read -r -d R -a pos
          |stty $oldstty
          |row=$((${pos[0]:2} - 1))
          |col=$((${pos[1]} - 1))
          |echo $row $col""".stripMargin
      Files.write(scriptFile.toPath, script.getBytes("UTF-8"))
      val pb = new ProcessBuilder("bash", "-c", s"chmod +x ${scriptFile.getCanonicalPath}")
      pb.start().waitFor()
    }
    val pb = new ProcessBuilder("bash", "-c", scriptFile.getCanonicalPath)
    val p = pb.start()
    val i = new BufferedReader(new InputStreamReader(p.getInputStream))
    try {
      val line = i.readLine()
      val (row, col) = line.splitAt(line.indexOf(' '))
      (row.trim.toInt, col.trim.toInt)
    } finally {
      i.close()
    }
  }.getOrElse((-1, -1))

  override def executionContext: ExecutionContext = ExecutionContext.global
}