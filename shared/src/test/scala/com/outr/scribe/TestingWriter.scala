package com.outr.scribe

import com.outr.scribe.formatter.Formatter
import com.outr.scribe.writer.Writer

import scala.collection.mutable.ListBuffer

object TestingWriter extends Writer {
  val records = ListBuffer.empty[LogRecord]

  def write(record: LogRecord, formatter: Formatter): Unit = records += record

  def clear(): Unit = records.clear()
}