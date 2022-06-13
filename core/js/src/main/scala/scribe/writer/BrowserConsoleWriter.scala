package scribe.writer

import scribe.Platform._
import scribe._
import scribe.output._
import scribe.output.format.OutputFormat

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.scalajs.js

/**
  * Writer specifically to target the JavaScript console in the browser
  */
object BrowserConsoleWriter extends Writer {
  val args: ListBuffer[String] = ListBuffer.empty

  override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = {
    val b = new mutable.StringBuilder
    args.clear()
    outputFormat.begin(b.append(_))
    outputFormat(output, b.append(_))
    outputFormat.end(b.append(_))

    val jsArgs = args.map(js.Any.fromString).toList

    if (record.level >= Level.Error) {
      console.error(b.toString(), jsArgs: _*)
    } else if (record.level >= Level.Warn) {
      console.warn(b.toString(), jsArgs: _*)
    } else {
      console.log(b.toString(), jsArgs: _*)
    }
  }
}