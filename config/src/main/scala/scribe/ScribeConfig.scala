package scribe

import moduload.Moduload
import profig.Profig

import scala.concurrent.{ExecutionContext, Future}

object ScribeConfig extends Moduload {
  private var _loaded: Boolean = false
  def loaded: Boolean = _loaded

  override def load()(implicit ec: ExecutionContext): Future[Unit] = Profig.initConfiguration().map { _ =>
    val config = Profig("scribe").as[ScribeConfig]
    // TODO: Support
    _loaded = true
  }

  override def error(t: Throwable): Unit = scribe.error("Error while loading scribe-config", t)
}

case class ScribeConfig()