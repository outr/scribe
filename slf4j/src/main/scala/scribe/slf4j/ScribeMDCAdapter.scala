package scribe.slf4j

import java.util

import org.slf4j.spi.MDCAdapter
import scribe.MDC
import scala.jdk.CollectionConverters._

object ScribeMDCAdapter extends MDCAdapter {
  override def put(key: String, `val`: String): Unit = MDC(key) = `val`

  override def get(key: String): String = MDC.get(key).orNull

  override def remove(key: String): Unit = MDC.remove(key)

  override def clear(): Unit = MDC.clear()

  override def getCopyOfContextMap: util.Map[String, String] = MDC.map.map {
    case (key, function) => key -> function()
  }.asJava

  override def setContextMap(contextMap: util.Map[String, String]): Unit = {
    clear()
    contextMap.asScala.foreach {
      case (key, value) => put(key, value)
    }
  }
}