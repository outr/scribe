package scribe.mdc

trait MDCManager {
  def instance: MDC

  def instance_=(mdc: MDC): Unit
}