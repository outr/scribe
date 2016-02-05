package com.outr.scribe.formatter

import com.outr.scribe.LogRecord

trait Formatter {
  def format(record: LogRecord): String
}

object Formatter {
//  val Default = FormatterBuilder.parse("${date} [${threadName}] ${levelPaddedRight} ${classNameAbbreviated} - ${message}${newLine}")
  val Advanced = FormatterBuilder().
    date().
    string(" [").threadName.string("] ").
    levelPaddedRight.string(" ").
    classNameAbbreviated.string(".").methodName.string(":").lineNumber.
    string(" - ").message.newLine
}