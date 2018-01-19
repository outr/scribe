package scribe

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

object Macros {
  case class EnclosingType(className: String, methodName: Option[String])

  def enclosingType(c: whitebox.Context): EnclosingType = {
    import c.universe._

    val term = c.internal.enclosingOwner.asTerm match {
      case t if t.isMethod => t
      case t if t.owner.isMethod => t.owner
      case t => t
    }
    val className = term.owner.fullName
    val methodName = if (term.isMethod) Some(term.asMethod.name.decodedName.toString) else None
    EnclosingType(className, methodName)
  }

  def loggerByEnclosingType(c: whitebox.Context): c.universe.Tree = {
    import c.universe._

    val EnclosingType(className, _) = enclosingType(c)
    q"scribe.Logger.byName($className)"
  }

  def log(c: whitebox.Context)(level: c.Expr[Level], message: c.Tree): c.universe.Tree = {
    import c.universe._

    logSpecial(c)(level, message, q"scribe.LogRecord.DefaultStringify")
  }

  def logSpecial(c: whitebox.Context)(level: c.Expr[Level], message: c.Tree, stringify: c.Tree): c.universe.Tree = {
    import c.universe._

    val logger = c.prefix.tree
    val EnclosingType(className, methodName) = enclosingType(c)
    val line = c.enclosingPosition.line
    q"$logger.log($level, $message, $className, $methodName, $line, $stringify)"
  }

  def trace(c: whitebox.Context)(message: c.Tree): c.universe.Tree = log(c)(c.universe.reify(Level.Trace), message)

  def traceThrowable(c: whitebox.Context)(t: c.Tree): c.universe.Tree = {
    import c.universe._
    logSpecial(c)(c.universe.reify(Level.Trace), t, q"(v: Any) => scribe.Logger.throwable2String(v.asInstanceOf[Throwable])")
  }

  def traceMessageAndThrowable(c: whitebox.Context)(message: c.Tree, t: c.Tree): c.universe.Tree = {
    import c.universe._
    logSpecial(c)(c.universe.reify(Level.Trace), q"($message, $t)", q"""{ case (m: String, t: Throwable) => scribe.Logger.messageAndThrowable2String(m, t) }""")
  }

  def debug(c: whitebox.Context)(message: c.Tree): c.universe.Tree = log(c)(c.universe.reify(Level.Debug), message)

  def debugThrowable(c: whitebox.Context)(t: c.Tree): c.universe.Tree = {
    import c.universe._
    logSpecial(c)(c.universe.reify(Level.Debug), t, q"(v: Any) => scribe.Logger.throwable2String(v.asInstanceOf[Throwable])")
  }

  def debugMessageAndThrowable(c: whitebox.Context)(message: c.Tree, t: c.Tree): c.universe.Tree = {
    import c.universe._
    logSpecial(c)(c.universe.reify(Level.Debug), q"($message, $t)", q"""{ case (m: String, t: Throwable) => scribe.Logger.messageAndThrowable2String(m, t) }""")
  }

  def info(c: whitebox.Context)(message: c.Tree): c.universe.Tree = log(c)(c.universe.reify(Level.Info), message)

  def infoThrowable(c: whitebox.Context)(t: c.Tree): c.universe.Tree = {
    import c.universe._
    logSpecial(c)(c.universe.reify(Level.Info), t, q"(v: Any) => scribe.Logger.throwable2String(v.asInstanceOf[Throwable])")
  }

  def infoMessageAndThrowable(c: whitebox.Context)(message: c.Tree, t: c.Tree): c.universe.Tree = {
    import c.universe._
    logSpecial(c)(c.universe.reify(Level.Info), q"($message, $t)", q"""{ case (m: String, t: Throwable) => scribe.Logger.messageAndThrowable2String(m, t) }""")
  }

  def warn(c: whitebox.Context)(message: c.Tree): c.universe.Tree = log(c)(c.universe.reify(Level.Warn), message)

  def warnThrowable(c: whitebox.Context)(t: c.Tree): c.universe.Tree = {
    import c.universe._
    logSpecial(c)(c.universe.reify(Level.Warn), t, q"(v: Any) => scribe.Logger.throwable2String(v.asInstanceOf[Throwable])")
  }

  def warnMessageAndThrowable(c: whitebox.Context)(message: c.Tree, t: c.Tree): c.universe.Tree = {
    import c.universe._
    logSpecial(c)(c.universe.reify(Level.Warn), q"($message, $t)", q"""{ case (m: String, t: Throwable) => scribe.Logger.messageAndThrowable2String(m, t) }""")
  }

  def error(c: whitebox.Context)(message: c.Tree): c.universe.Tree = log(c)(c.universe.reify(Level.Error), message)

  def errorThrowable(c: whitebox.Context)(t: c.Tree): c.universe.Tree = {
    import c.universe._
    logSpecial(c)(c.universe.reify(Level.Error), t, q"(v: Any) => scribe.Logger.throwable2String(v.asInstanceOf[Throwable])")
  }

  def errorMessageAndThrowable(c: whitebox.Context)(message: c.Tree, t: c.Tree): c.universe.Tree = {
    import c.universe._
    logSpecial(c)(c.universe.reify(Level.Error), q"($message, $t)", q"""{ case (m: String, t: Throwable) => scribe.Logger.messageAndThrowable2String(m, t) }""")
  }
}
