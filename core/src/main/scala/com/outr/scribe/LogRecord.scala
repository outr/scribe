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

object LogRecord {
  def trace(name: String, level: Level): (Option[String], Option[Int]) = {
    val stackTrace = Thread.currentThread().getStackTrace
    val elementOption = stackTrace.find(ste => isValid(ste, level))
    val element = elementOption.getOrElse(
      throw new NullPointerException(s"Unable to find $name in stack trace (${stackTrace.map(ste => ste.getClassName).mkString(", ")}})!")
    )
    val methodName = Some(element.getMethodName)
    val lineNumber = Some(element.getLineNumber)
    (methodName, lineNumber)
  }

  private val classExclusions = Set(
    classOf[Thread],
    classOf[LogRecord],
    classOf[Logger]
  ).flatMap(c => Set(c.getName, s"${c.getName}$$"))

  private def isValid(ste: StackTraceElement, level: Level): Boolean = {
    !classExclusions.contains(ste.getClassName)
  }
}