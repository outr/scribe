import scala.language.experimental.macros
import scala.language.implicitConversions

package object scribe {
  protected[scribe] var disposables = Set.empty[() => Unit]

  def dispose(): Unit = disposables.foreach(d => d())
}