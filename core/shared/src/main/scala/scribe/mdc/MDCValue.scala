package scribe.mdc

case class MDCValue(value: () => Any) extends AnyVal