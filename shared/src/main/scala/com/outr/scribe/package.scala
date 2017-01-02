package com.outr

import scala.language.experimental.macros

package object scribe {
  def logger: Logger = macro Macros.loggerByEnclosingType

  implicit class AnyLogging(value: Any) {
    def logger: Logger = Logger.byName(value.getClass.getSimpleName)
  }
}