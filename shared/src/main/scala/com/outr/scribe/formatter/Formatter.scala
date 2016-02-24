package com.outr.scribe.formatter

import com.outr.scribe.LogRecord

trait Formatter {
  def format(record: LogRecord): String
}

object Formatter {
  val Simple = FormatterBuilder().message.newLine
  val Default = FormatterBuilder().
    date().
    string(" [").threadName.string("] ").
    levelPaddedRight.string(" ").
    classNameAbbreviated.string(".").methodName.string(":").lineNumber.
    string(" - ").message.newLine

  val Trace = FormatterBuilder().
    threadName.string("-").levelPaddedRight.string("-")
    .classNameAbbreviated.string(".").methodName.string(":").lineNumber
    .string("-").message.newLine
}