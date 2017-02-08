package org.slf4j.impl

import scribe.slf4j.ScribeLoggerFactory
import org.slf4j.ILoggerFactory
import org.slf4j.spi.LoggerFactoryBinder

class StaticLoggerBinder private() extends LoggerFactoryBinder {
  private val factory = new ScribeLoggerFactory
  private val classString = classOf[ScribeLoggerFactory].getName

  override def getLoggerFactory: ILoggerFactory = factory

  override def getLoggerFactoryClassStr: String = classString
}

object StaticLoggerBinder extends StaticLoggerBinder {
  val REQUESTED_API_VERSION = "1.7.15"

  def getSingleton: StaticLoggerBinder = this
}