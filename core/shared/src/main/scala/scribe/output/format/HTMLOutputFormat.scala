package scribe.output.format

import scribe.output._

case class HTMLOutputFormat(style: HTMLStyle = SimpleHTMLStyle) extends OutputFormat {
  override def init(stream: String => Unit): Unit = {
    stream(
      s"""<html>
        |<head>
        |${style.head}
        |</head>
        |<body>
        |""".stripMargin)
  }

  override def apply(output: LogOutput, stream: String => Unit): Unit = wrapped(stream, "div", output, "class" -> "record")

  def recurse(output: LogOutput, stream: String => Unit): Unit = output match {
    case o: TextOutput => stream(o.plainText.replace("\n", "<br/>").replace(" ", "&#160;"))
    case o: CompositeOutput => o.entries.foreach(recurse(_, stream))
    case o: ColoredOutput => wrapped(stream, "span", o.output, style.fgMapping(o.color))
    case o: BackgroundColoredOutput => wrapped(stream, "span", o.output, style.bgMapping(o.color))
    case o: URLOutput => wrapped(stream, "a", o.output, "href" -> o.url)
    case o: BoldOutput => wrapped(stream, "strong", o.output)
    case o: ItalicOutput => wrapped(stream, "em", o.output)
    case o: UnderlineOutput => wrapped(stream, "u", o.output)
    case o: StrikethroughOutput => wrapped(stream, "strike", o.output)
    case _ => stream(output.plainText)
  }

  private def wrapped(stream: String => Unit, tagName: String, output: LogOutput, attributes: (String, String)*): Unit = {
    stream("<")
    stream(tagName)
    attributes.foreach {
      case (key, value) => {
        stream(" ")
        stream(key)
        stream("=\"")
        stream(value)
        stream("\"")
      }
    }
    stream(">")
    recurse(output, stream)
    stream("</")
    stream(tagName)
    stream(">")
  }
}

trait HTMLStyle {
  def head: String
  def fgMapping(color: Color): (String, String)
  def bgMapping(color: Color): (String, String)
}

object SimpleHTMLStyle extends HTMLStyle {
  override def head: String = ""

  override def fgMapping(color: Color): (String, String) = {
    "style" -> s"color: ${color.name}"
  }

  override def bgMapping(color: Color): (String, String) = {
    "style" -> s"background-color: ${color.name}"
  }
}

object SolarizedDark extends HTMLStyle {
  val base03: String = "#002b36"
  val base02: String = "#073642"
  val base01: String = "#586e75"
  val base00: String = "#657b83"
  val base0: String = "#839496"
  val base1: String = "#93a1a1"
  val base2: String = "#eee8d5"
  val base3: String = "#fdf6e3"
  val yellow: String = "#b58900"
  val orange: String = "#cb4b16"
  val red: String = "#dc322f"
  val magenta: String = "#d33682"
  val violet: String = "#6c71c4"
  val blue: String = "#268bd2"
  val cyan: String = "#2aa198"
  val green: String = "#859900"

  private val list = List(
    "black" -> base02,
    "blue" -> blue,
    "cyan" -> cyan,
    "green" -> green,
    "magenta" -> magenta,
    "red" -> red,
    "white" -> base2,
    "yellow" -> yellow,
    "gray" -> base1,
    "brightblue" -> base0,
    "brightcyan" -> base1,
    "brightgreen" -> base01,
    "brightmagenta" -> violet,
    "brightred" -> orange,
    "brightwhite" -> base3,
    "brightyellow" -> base00
  )

  private val colorClasses = list.map {
    case (name, hex) =>
      s""".${name}FG {
         |  color: $hex
         |}
         |.${name}BG {
         |  background-color: $hex
         |}""".stripMargin
  }.mkString("\n")

  override val head: String = s"""<link rel="preconnect" href="https://fonts.googleapis.com">
                                 |<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                                 |<link href="https://fonts.googleapis.com/css2?family=Inconsolata&display=swap" rel="stylesheet">
                                 |<style>
                                 |* {
                                 |  font-family: 'Inconsolata', monospace;
                                 |  color: white;
                                 |}
                                 |body {
                                 |  background-color: $base03
                                 |}
                                 |$colorClasses
                                 |</style>
                                 |""".stripMargin

  override def fgMapping(color: Color): (String, String) = ("class", s"${color.name}FG")

  override def bgMapping(color: Color): (String, String) = ("class", s"${color.name}BG")
}