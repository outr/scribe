package com.outr.scribe.formatter

import com.outr.scribe.{Platform, LogRecord}

case class FormatterBuilder(formatters: List[LogRecord => String] = Nil) extends Formatter {
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

  def add(item: LogRecord => String): FormatterBuilder = copy(formatters = formatters ++ Seq(item))

  def format(record: LogRecord): String =
    formatters.foldLeft("") { case (str, f) => str + f(record) }
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
  val ClassNameAbbreviated = (record: LogRecord) => abbreviate(record.name)
  val MethodName = (record: LogRecord) => record.methodName.getOrElse("Unknown method")
  val LineNumber = (record: LogRecord) => record.lineNumber.toString
  val Message: LogRecord => String = (record: LogRecord) => String.valueOf(record.message)
  val NewLine: LogRecord => String = (record: LogRecord) => Platform.LineSeparator

  final def abbreviate(className: String): String = {
    val parts = className.split('.')
    val last = parts.length - 1
    parts.zipWithIndex.map {
      case (cur, i) if i == last => cur
      case (cur, _)              => cur.head
    }.mkString(".")
  }
}