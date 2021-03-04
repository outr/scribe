package scribe

import moduload.Moduload
import fabric.rw._
import profig._

object ScribeConfig extends Moduload {
  implicit def rw: ReaderWriter[ScribeConfig] = ccRW

  private var _loaded: Boolean = false
  def loaded: Boolean = _loaded

  override def load(): Unit = {
    Profig.initConfiguration()
//    val config = Profig("scribe").as[ScribeConfig]
    // TODO: Support
    _loaded = true
  }

  override def error(t: Throwable): Unit = scribe.error("Error while loading scribe-config", t)
}

case class ScribeConfig()