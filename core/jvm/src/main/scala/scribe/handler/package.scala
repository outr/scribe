package scribe

import java.util.concurrent.atomic.AtomicLong
import scala.language.implicitConversions

package object handler {
  implicit def atomicExtras(l: AtomicLong): AtomicLongExtras = new AtomicLongExtras(l)
}