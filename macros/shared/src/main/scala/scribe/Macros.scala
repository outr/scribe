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

  def getLevel(c: blackbox.Context): c.Tree = {
    import c.universe._

    c.macroApplication.symbol.name.decodedName.toString match {
      case "trace" => q"scribe.Level.Trace"
      case "debug" => q"scribe.Level.Debug"
      case "info" => q"scribe.Level.Info"
      case "warn" => q"scribe.Level.Warn"
      case "error" => q"scribe.Level.Error"
    }
  }

  def autoLevel[M](c: blackbox.Context)(message: c.Expr[M])(stringify: c.Expr[Loggable[M]])
                  (implicit m: c.WeakTypeTag[M]): c.Tree = {
    import c.universe._

    val level = getLevel(c)
    log(c)(level, message, reify[Option[Throwable]](None))(stringify)
  }

  def autoLevel2[M](c: blackbox.Context)(message: c.Expr[M], t: c.Expr[Throwable])(stringify: c.Expr[Loggable[M]])
                   (implicit m: c.WeakTypeTag[M]): c.Tree = {
    import c.universe._

    val level = getLevel(c)
    log(c)(level, message, c.Expr[Option[Throwable]](q"Option($t)"))(stringify)
  }

  def log[M](c: blackbox.Context)
            (level: c.Tree,
             message: c.Expr[M],
             throwable: c.Expr[Option[Throwable]])
            (stringify: c.Expr[Loggable[M]])
            (implicit m: c.WeakTypeTag[M]): c.Tree = {
    import c.universe._

    val logger = c.prefix.tree
    val EnclosingType(className, methodName) = enclosingType(c)
    val line = c.enclosingPosition.line match {
      case -1 => None
      case n => Some(n)
    }
    val fileName = c.enclosingPosition.source.path
    val dcn = if (logger.tpe.toString == "scribe.Logger") {
      q"$logger.overrideClassName.getOrElse($className)"
    } else {
      q"$className"
    }
    val dmn = if (logger.tpe.toString == "scribe.Logger") {
      q"if ($logger.overrideClassName.nonEmpty) None else $methodName"
    } else {
      q"$methodName"
    }
    val dln = if (logger.tpe.toString == "scribe.Logger") {
      q"if ($logger.overrideClassName.nonEmpty) None else $line"
    } else {
      q"$line"
    }

    q"$logger.log(scribe.LogRecord[$m]($level, $level.value, $message, $stringify, $throwable, $fileName, $dcn, $dmn, $dln))"
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
