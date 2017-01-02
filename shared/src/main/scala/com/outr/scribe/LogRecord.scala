package com.outr.scribe

class LogRecord private() {
  private var _level: Level = _
  private var _value: Double = _
  private var _messageFunction: () => Any = _
  private var _className: String = _
  private var _methodName: Option[String] = None
  private var _lineNumber: Int = _
  private var _threadId: Long = _
  private var _threadName: String = _
  private var _timestamp: Long = _

  private var _message: Option[String] = None

  def level: Level = _level
  def value: Double = _value
  def message: String = _message match {
    case Some(m) => m
    case None => {
      val m = String.valueOf(_messageFunction())
      _message = Option(m)
      m
    }
  }
  def className: String = _className
  def methodName: Option[String] = _methodName
  def lineNumber: Int = _lineNumber
  def threadId: Long = _threadId
  def threadName: String = _threadName
  def timestamp: Long = _timestamp

  def updateValue(value: Double): LogRecord = {
    _value = value
    this
  }

  override def toString: String = {
    val list = List(
      "level" -> level.name,
      "value" -> value.toString,
      "class" -> className,
      "method" -> methodName.getOrElse("???"),
      "line" -> lineNumber.toString,
      "message" -> message
    )
    list.map(t => s"${t._1}: ${t._2}").mkString("LogRecord(", ", ", ")")
  }
}

object LogRecord {
  private val instance = new ThreadLocal[LogRecord] {
    override def initialValue(): LogRecord = new LogRecord()
  }

  def apply(level: Level,
            value: Double,
            message: () => Any,
            className: String,
            methodName: Option[String],
            lineNumber: Int,
            threadId: Long = Thread.currentThread().getId,
            threadName: String = Thread.currentThread().getName,
            timestamp: Long = System.currentTimeMillis()): LogRecord = {
    val r = instance.get()
    r._level = level
    r._value = value
    r._messageFunction = message
    r._className = className
    r._methodName = methodName
    r._lineNumber = lineNumber
    r._threadId = threadId
    r._threadName = threadName
    r._timestamp = timestamp
    r._message = None
    r
  }
}