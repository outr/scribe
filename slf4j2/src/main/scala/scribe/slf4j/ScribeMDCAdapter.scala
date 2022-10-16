package scribe.slf4j

import org.slf4j.spi.MDCAdapter
import scribe.data.MDC

import java.util
import scala.jdk.CollectionConverters._

object ScribeMDCAdapter extends MDCAdapter {
  override def put(key: String, `val`: String): Unit = MDC(key) = `val`

  override def get(key: String): String = MDC.get(key).map(_.toString).orNull

  override def remove(key: String): Unit = MDC.remove(key)

  override def clear(): Unit = MDC.clear()

  override def getCopyOfContextMap: util.Map[String, String] = MDC.map.map {
    case (key, function) => key -> function().toString
  }.asJava

  override def setContextMap(contextMap: util.Map[String, String]): Unit = {
    clear()
    contextMap.asScala.foreach {
      case (key, value) => put(key, value)
    }
  }

  // TODO: Support stacking
  override def pushByKey(key: String, value: String): Unit = put(key, value)

  override def popByKey(key: String): String = {
    val value = get(key)
    remove(key)
    value
  }

  override def getCopyOfDequeByKey(key: String): util.Deque[String] = ???

  override def clearDequeByKey(key: String): Unit = remove(key)
}