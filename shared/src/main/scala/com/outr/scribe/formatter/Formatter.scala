package com.outr.scribe.formatter

import com.outr.scribe.LogRecord

trait Formatter {
  def format(record: LogRecord): String
}

object Formatter {
  val simple: FormatterBuilder = FormatterBuilder().message.newLine
  val default: FormatterBuilder = FormatterBuilder()
    .date()
    .string(" [").threadName.string("] ")
    .levelPaddedRight.string(" ")
    .positionAbbreviated
    .string(" - ").message.newLine
  val trace: FormatterBuilder = FormatterBuilder()
    .threadName.string("-").levelPaddedRight.string("-")
    .positionAbbreviated
    .string("-").message.newLine
}