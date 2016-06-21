package com.outr.scribe.formatter

import com.outr.scribe.LogRecord

trait Formatter {
  def format(record: LogRecord): String
}

object Formatter {
  val simple = FormatterBuilder().message.newLine
  val default = FormatterBuilder().
    date().
    string(" [").threadName.string("] ").
    levelPaddedRight.string(" ").
    classNameAbbreviated.string(".").methodName.string(":").lineNumber.
    string(" - ").message.newLine
  val trace = FormatterBuilder().
    threadName.string("-").levelPaddedRight.string("-")
    .classNameAbbreviated.string(".").methodName.string(":").lineNumber
    .string("-").message.newLine
}