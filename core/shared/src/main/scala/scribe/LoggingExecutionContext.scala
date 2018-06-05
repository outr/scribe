package scribe

import scala.concurrent.ExecutionContext

class LoggingExecutionContext(context: ExecutionContext, stack: List[Position]) extends ExecutionContext {
  override def execute(runnable: Runnable): Unit = {
    val mdc = MDC.instance
    val external = Position.stack
    val r = new Runnable {
      override def run(): Unit = {
        val previous = Position.stack
        Position.stack = previous ::: external ::: stack
        try {
          MDC.contextualize(mdc) {
            try {
              runnable.run()
            } catch {
              case t: Throwable => {      // TODO: remove since this never gets called
                println("MODIFYING TRACE!")
                val trace = Position.stack.map(_.toTraceElement) ++ t.getStackTrace
                t.setStackTrace(trace.toArray)
                throw t
              }
            }
          }
        } finally {
          Position.stack = previous
        }
      }
    }
    context.execute(r)
  }

  override def reportFailure(cause: Throwable): Unit = {        // TODO: remove since this never gets called
    println("REPORT FAILURE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
    context.reportFailure(cause)
  }
}