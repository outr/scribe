package scribe

import scala.concurrent.ExecutionContext

object Execution {
  implicit def global: ExecutionContext = Platform.executionContext
}