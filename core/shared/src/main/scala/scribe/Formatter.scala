package scribe2

trait Formatter {
  def format(record: LogRecord): String
}

object Formatter {
  object simple extends FormatterBuilder {
    override def apply(): String = s"$message$newLine"
  }

  object default extends FormatterBuilder {
    override def apply(): String = {
      s"$date [$threadName] $levelPaddedRight $positionAbbreviated - $message$newLine"
    }
  }
}

trait FormatterBuilder extends Formatter {
  private[scribe2] var record: LogRecord = _

  override def format(record: LogRecord): String = {
    this.record = record
    apply()
  }

  def date: String = {
    val ts = record.timeStamp
    f"$ts%tY.$ts%tm.$ts%td $ts%tT:$ts%tL"
  }

  def threadName: String = record.thread.getName

  def levelPaddedRight: String = record.level.namePaddedRight

  def position: String = {
    val b = new StringBuilder
    b.append(record.className)
    record.methodName.foreach { methodName =>
      b.append('.')
      b.append(methodName)
    }
    record.lineNumber.foreach { ln =>
      b.append(':')
      b.append(ln)
    }
    b.toString()
  }

  def positionAbbreviated: String = {
    val b = new StringBuilder
    b.append(FormatterBuilder.abbreviate(record.className))
    record.methodName.foreach { methodName =>
      b.append('.')
      b.append(methodName)
    }
    record.lineNumber.foreach { ln =>
      b.append(':')
      b.append(ln)
    }
    b.toString()
  }

  def message: String = record.message

  def newLine: String = "\n"

  def apply(): String
}