package scribe.output.format

import scribe.output.{BackgroundColoredOutput, BoldOutput, ColoredOutput, CompositeOutput, ItalicOutput, LogOutput, StrikethroughOutput, TextOutput, URLOutput, UnderlineOutput}

object HTMLOutputFormat extends OutputFormat {
  override def apply(output: LogOutput, stream: String => Unit): Unit = wrapped(stream, "div", output, "class" -> "record")

  def recurse(output: LogOutput, stream: String => Unit): Unit = output match {
    case o: TextOutput => stream(o.plainText)
    case o: CompositeOutput => o.entries.foreach(recurse(_, stream))
    case o: ColoredOutput => {
      val color = o.color.getClass.getSimpleName.replace("$", "").toLowerCase
      wrapped(stream, "span", o.output, "style" -> s"color: $color")
    }
    case o: BackgroundColoredOutput => {
      val color = o.color.getClass.getSimpleName.replace("$", "").toLowerCase
      wrapped(stream, "span", o.output, "style" -> s"background-color: $color")
    }
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