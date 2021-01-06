package scribe

import scala.concurrent.ExecutionContext

object Execution {
  implicit def global: ExecutionContext = scala.concurrent.ExecutionContext.global
}
