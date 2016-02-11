package com.scribe.slf4j

import org.slf4j.{Logger, ILoggerFactory}

class ScribeLoggerFactory extends ILoggerFactory {
  override def getLogger(name: String): Logger = ???
}
