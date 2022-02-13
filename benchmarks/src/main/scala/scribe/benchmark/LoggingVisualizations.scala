package scribe.benchmark

import scribe.benchmark.tester.{LoggingTester, Testers}

import java.util.concurrent.TimeUnit
import perfolation._

import java.io.{BufferedWriter, File, FileOutputStream, OutputStreamWriter}
import scala.annotation.tailrec
import scala.io.Source

object LoggingVisualizations {
  val Iterations: Int = 10_000_000

  private lazy val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("results.txt"), true)))

  def main(args: Array[String]): Unit = {
    val testers = new Testers
    val list = args.toList match {
      case name :: Nil => List(testers.all.find(_.name.equalsIgnoreCase(name)).getOrElse(throw new RuntimeException(s"Unable to find $name")))
      case Nil => testers.all
      case list => sys.error(s"Expected zero or one argument, got: $list")
    }
    list.foreach { tester =>
      benchmark(tester)
    }
  }

  def logFiles(waitForFinished: Boolean): List[File] = {
    val logs = new File("logs")
    if (logs.isDirectory) {
      def list = logs.listFiles().toList
      var waited = 0
      while (list.isEmpty && waitForFinished && waited < 10) {
        waited += 1
        Thread.sleep(250)
      }
      if (list.nonEmpty && waitForFinished) {
        @tailrec
        def hasChanges(lastChanges: Long): Unit = {
          val lastModified = list.map(_.lastModified()).max
          if (lastModified == lastChanges) {
            // Finished
          } else {
            Thread.sleep(1000L)
            hasChanges(lastModified)
          }
        }
        hasChanges(0L)
      }
      list
    } else {
      Nil
    }
  }

  def linesFor(file: File): Int = {
    val source = Source.fromFile(file)
    try {
      source.getLines().length
    } finally {
      source.close()
    }
  }

  def deleteLogs(): Unit = logFiles(false).foreach { f =>
    if (!f.delete()) {
      println(s"${f.getName} not able to be deleted")
    }
  }

  def benchmark(tester: LoggingTester): Unit = {
    deleteLogs()
    val initTime = elapsed(tester.init())
    val messages = (0 until Iterations).map(i => s"visualize $i").iterator
    val runTime = elapsed(tester.run(messages))
    val disposeTime = elapsed(tester.dispose())
    println(s"${tester.name}: Init: $initTime, Run: $runTime, Dispose: $disposeTime")
    var fileLines = Map.empty[String, Int]
    val filesTime = elapsed {
      val logs = logFiles(true)
      logs.foreach { file =>
        if (file.length() > 0L) {
          val lines = linesFor(file)
          println(s"\t${file.getName} created with $lines lines")
          fileLines += file.getName -> lines
        }
      }
      if (logs.isEmpty) System.err.println("*** NO LOG FILES CREATED")
    }
    println(s"\tFiles: $filesTime")
    writer.write(s"${tester.name}:\n")
    writer.write(s"\tInit: $initTime, Run: $runTime, Files: $filesTime, Lines: ${fileLines.map(t => s"${t._1}: ${t._2}").mkString(", ")}, Dispose: $disposeTime\n")
    writer.flush()
  }

  private def elapsed(f: => Unit): String = {
    val start = System.nanoTime()
    f
    val elapsed = System.nanoTime() - start
    val ms = TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS)
    s"${(ms / 1000.0).f(f = 3)} seconds"
  }
}