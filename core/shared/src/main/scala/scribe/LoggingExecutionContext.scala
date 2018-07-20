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
            runnable.run()
          }
        } finally {
          Position.stack = previous
        }
      }
    }
    context.execute(r)
  }

  override def reportFailure(cause: Throwable): Unit = {
    context.reportFailure(cause)
  }
}