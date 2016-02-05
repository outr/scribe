package com.outr.scribe

case class LogRecord(name: String,
                     level: Level,
                     value: Double,
                     message: () => Any,
                     methodName: Option[String] = None,
                     lineNumber: Option[Int] = None,
                     threadId: Long = Thread.currentThread().getId,
                     threadName: String = Thread.currentThread().getName,
                     timestamp: Long = System.currentTimeMillis())
