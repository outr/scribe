package scribe.data

object MDCThreadLocal extends MDCManager {
  private val threadLocal: InheritableThreadLocal[MDC] = new InheritableThreadLocal[MDC] {
    override def initialValue(): MDC = MDC.creator(Some(MDC.global))

    override def childValue(parentValue: MDC): MDC = MDC.creator(Option(parentValue).orElse(Some(MDC.global)))
  }

  override def instance: MDC = threadLocal.get()

  override def instance_=(mdc: MDC): Unit = threadLocal.set(mdc)
}