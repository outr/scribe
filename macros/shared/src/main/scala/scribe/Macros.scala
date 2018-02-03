package scribe

import scala.annotation.compileTimeOnly
import scala.collection.mutable.ListBuffer
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

@compileTimeOnly("Enable macros to expand")
object Macros {
  def formatter(c: blackbox.Context)(args: c.Tree*): c.Tree = {
    import c.universe._

    c.prefix.tree match {
      case Apply(_, List(Apply(_, rawParts))) => {
        val parts = rawParts map { case t @ Literal(Constant(const: String)) => (const, t.pos) }
        val list = ListBuffer.empty[c.Tree]
        val argsVector = args.toVector
        parts.zipWithIndex.foreach {
          case ((raw, _), index) => {
            if (raw.nonEmpty) {
              list += q"scribe.format.FormatBlock.RawString($raw)"
            }
            if (index < argsVector.size) {
              list += argsVector(index)
            }
          }
        }
        q"scribe.format.Formatter.fromBlocks(..$list)"
      }
      case _ => c.abort(c.enclosingPosition, "Bad usage of formatter interpolation.")
    }
  }

  def trace(c: blackbox.Context)(message: c.Tree): c.Tree = {
    import c.universe._

    log(c)(q"scribe.Level.Trace", message)
  }

  def debug(c: blackbox.Context)(message: c.Tree): c.Tree = {
    import c.universe._

    log(c)(q"scribe.Level.Debug", message)
  }

  def info(c: blackbox.Context)(message: c.Tree): c.Tree = {
    import c.universe._

    log(c)(q"scribe.Level.Info", message)
  }

  def warn(c: blackbox.Context)(message: c.Tree): c.Tree = {
    import c.universe._

    log(c)(q"scribe.Level.Warn", message)
  }

  def error(c: blackbox.Context)(message: c.Tree): c.Tree = {
    import c.universe._

    log(c)(q"scribe.Level.Error", message)
  }

  def trace2(c: blackbox.Context)(message: c.Tree, t: c.Tree): c.Tree = {
    import c.universe._

    log(c)(q"scribe.Level.Trace", message)
    log(c)(q"scribe.Level.Trace", t)
  }

  def debug2(c: blackbox.Context)(message: c.Tree, t: c.Tree): c.Tree = {
    import c.universe._

    log(c)(q"scribe.Level.Debug", message)
    log(c)(q"scribe.Level.Debug", t)
  }

  def info2(c: blackbox.Context)(message: c.Tree, t: c.Tree): c.Tree = {
    import c.universe._

    log(c)(q"scribe.Level.Info", message)
    log(c)(q"scribe.Level.Info", t)
  }

  def warn2(c: blackbox.Context)(message: c.Tree, t: c.Tree): c.Tree = {
    import c.universe._

    log(c)(q"scribe.Level.Warn", message)
    log(c)(q"scribe.Level.Warn", t)
  }

  def error2(c: blackbox.Context)(message: c.Tree, t: c.Tree): c.Tree = {
    import c.universe._

    log(c)(q"scribe.Level.Error", message)
    log(c)(q"scribe.Level.Error", t)
  }

  def log(c: blackbox.Context)(level: c.Tree, message: c.Tree): c.Tree = {
    import c.universe._

    val logger = c.prefix.tree
    val EnclosingType(className, methodName) = enclosingType(c)
    val line = c.enclosingPosition.line match {
      case -1 => None
      case n => Some(n)
    }
    val stringify = q"scribe.LogRecord.Stringify.Default"
    val derivedClassName = if (logger.tpe.toString == "scribe.Logger") {
      q"$logger.overrideClassName.getOrElse($className)"
    } else {
      q"$className"
    }

    q"$logger.log(scribe.LogRecord($level, $level.value, () => $message, $stringify, $derivedClassName, $methodName, $line))"
  }

  def enclosingType(c: blackbox.Context): EnclosingType = {
    val term = c.internal.enclosingOwner.asTerm match {
      case t if t.isMethod => t
      case t if t.owner.isMethod => t.owner
      case t => t
    }
    val className = term.owner.fullName
    val methodName = if (term.isMethod) Some(term.asMethod.name.decodedName.toString) else None
    EnclosingType(className, methodName)
  }

  case class EnclosingType(className: String, methodName: Option[String])
}