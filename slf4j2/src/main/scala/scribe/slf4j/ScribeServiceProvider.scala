package scribe.slf4j

import org.slf4j.helpers.BasicMarkerFactory
import org.slf4j.spi.{MDCAdapter, SLF4JServiceProvider}
import org.slf4j.{ILoggerFactory, IMarkerFactory}

class ScribeServiceProvider extends SLF4JServiceProvider {
  private lazy val markerFactory = new BasicMarkerFactory

  override def getLoggerFactory: ILoggerFactory = ScribeLoggerFactory

  override def getMarkerFactory: IMarkerFactory = markerFactory

  override def getMDCAdapter: MDCAdapter = ScribeMDCAdapter

  override def getRequestedApiVersion: String = "2.0.11"

  override def initialize(): Unit = {}
}