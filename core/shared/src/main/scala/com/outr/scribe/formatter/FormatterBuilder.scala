package com.outr.scribe.formatter

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

import com.outr.scribe.{Platform, LogRecord}

case class FormatterBuilder(items: List[LogRecord => String] = Nil) extends Formatter {
  def string(s: String): FormatterBuilder = add(FormatterBuilder.Static(s))

  def message: FormatterBuilder = add(FormatterBuilder.Message)

  def date(format: String = "%1$tY.%1$tm.%1$td %1$tT:%1$tL"): FormatterBuilder = add(FormatterBuilder.Date(format))

  def threadName: FormatterBuilder = add(FormatterBuilder.ThreadName)

  def level: FormatterBuilder = add(FormatterBuilder.Level)
  def levelPaddedRight: FormatterBuilder = add(FormatterBuilder.LevelPaddedRight)

  def className: FormatterBuilder = add(FormatterBuilder.ClassName)
  def classNameAbbreviated: FormatterBuilder = add(FormatterBuilder.ClassNameAbbreviated)

  def methodName: FormatterBuilder = add(FormatterBuilder.MethodName)
  def lineNumber: FormatterBuilder = add(FormatterBuilder.LineNumber)

  def newLine: FormatterBuilder = add(FormatterBuilder.NewLine)

  def add(item: LogRecord => String): FormatterBuilder = copy(items = items ++ Seq(item))

  def format(record: LogRecord): String = {
    val b = new StringBuilder
    process(b, record, items)
    b.toString()
  }

  @tailrec
  private def process(b: StringBuilder, record: LogRecord, l: List[LogRecord => String]): Unit =
    if (l.nonEmpty) {
      b.append(l.head(record))
      process(b, record, l.tail)
    }
}

object FormatterBuilder {
  type FormatEntry = LogRecord => String

  private var map = Map.empty[String, String => FormatEntry]
  def add(name: String, f: String => FormatEntry): Unit = synchronized {
    map += name -> f
  }

  add("string", (s: String) => Static(s))
  add("date", (s: String) => Option(s).map(Date).getOrElse(Date()))
  add("threadName", (s: String) => ThreadName)
  add("level", (s: String) => Level)
  add("levelPaddedRight", (s: String) => LevelPaddedRight)
  add("className", (s: String) => ClassName)
  add("classNameAbbreviated", (s: String) => ClassNameAbbreviated)
  add("methodName", (s: String) => MethodName)
  add("lineNumber", (s: String) => LineNumber)
  add("message", (s: String) => Message)
  add("newLine", (s: String) => NewLine)

  def Static(s: String): LogRecord => String = (record: LogRecord) => s
  def Date(format: String = "%1$tY.%1$tm.%1$td %1$tT:%1$tL"): LogRecord => String = (record: LogRecord) =>
    Platform.formatDate(format, record.timestamp)

  val ThreadName: LogRecord => String = (record: LogRecord) => record.threadName
  val Level: LogRecord => String = (record: LogRecord) => record.level.name
  val LevelPaddedRight: LogRecord => String = (record: LogRecord) => record.level.namePaddedRight
  val ClassName = (record: LogRecord) => record.name
  val ClassNameAbbreviated = (record: LogRecord) => abbreviate(record.name.split("[.]").toList)
  val MethodName = (record: LogRecord) => record.methodName.getOrElse("Unknown method")
  val LineNumber = (record: LogRecord) => record.lineNumber.fold("???")(_.toString)
  val Message: LogRecord => String = (record: LogRecord) => String.valueOf(record.message())
  val NewLine: LogRecord => String = (record: LogRecord) => Platform.LineSeparator

//  private val regex: Regex = """\$\{(.*?)\}""".r

//  def parse(s: String): FormatterBuilder = {
//    val results = regex.findAllIn(s)
//    FormatterBuilder(processRecursive(results))
//  }

  private def processRecursive(iterator: Regex.MatchIterator,
                               list: ListBuffer[FormatEntry] = ListBuffer.empty[FormatEntry],
                               previousEnd: Int = 0): List[FormatEntry] = {
    if (!iterator.hasNext) {
      val after = iterator.source.subSequence(previousEnd, iterator.source.length())
      if (after.length() > 0) {
        list += FormatterBuilder.Static(after.toString)
      }
      list.toList
    } else {
      iterator.next()
      if (iterator.start > previousEnd) {
        val before = iterator.source.subSequence(previousEnd, iterator.start)
        list += FormatterBuilder.Static(before.toString)
      }
      val block = iterator.group(1)
      val separator = block.indexOf(':')
      val (name, value) = if (separator != -1) {
        (block.substring(0, separator), block.substring(separator + 1))
      } else {
        (block, null)
      }
      list += parseBlock(name, value)
      processRecursive(iterator, list, iterator.end)
    }
  }

  protected def parseBlock(name: String, value: String): FormatEntry = {
    map(name)(value)
  }

  @tailrec
  final def abbreviate(values: List[String], b: StringBuilder = new StringBuilder): String = {
    if (values.isEmpty) {
      b.toString()
    } else {
      if (values.tail.isEmpty) {
        b.append(values.head)
        b.toString()
      } else {
        b.append(values.head.charAt(0))
        b.append('.')
        abbreviate(values.tail, b)
      }
    }
  }
}