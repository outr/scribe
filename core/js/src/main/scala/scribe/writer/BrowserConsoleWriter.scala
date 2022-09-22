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
  private var map = Map.empty[String, String]
  private var argsList = List.empty[String]

  object args {
    def around[Return](t: (String, String))(f: => Return): Return = {
      this += t
      try {
        f
      } finally {
        this -= t._1
      }
    }
    private def append(): Unit = argsList = map.map {
      case (key, value) if key.startsWith("::") => value
      case (key, value) => s"$key: $value"
    }.mkString("; ") :: argsList
    def +=(t: (String, String)): Unit = {
      map += t
      append()
    }
    def -=(key: String): Unit = {
      map -= key
      append()
    }
  }

  override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = {
    val b = new mutable.StringBuilder
    map = Map.empty
    argsList = Nil
    outputFormat.begin(b.append(_))
    outputFormat(output, b.append(_))
    outputFormat.end(b.append(_))

    val jsArgs = argsList.map(js.Any.fromString).reverse
    if (record.level >= Level.Error) {
      console.error(b.toString(), jsArgs: _*)
    } else if (record.level >= Level.Warn) {
      console.warn(b.toString(), jsArgs: _*)
    } else {
      console.log(b.toString(), jsArgs: _*)
    }
  }
}