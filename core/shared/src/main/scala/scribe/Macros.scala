package scribe

import scala.annotation.compileTimeOnly
import scala.concurrent.ExecutionContext
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

@compileTimeOnly("Enable macros to expand")
object Macros {
  def getLevel(c: blackbox.Context): c.Tree = {
    import c.universe._

    c.macroApplication.symbol.name.decodedName.toString match {
      case "trace" => q"_root_.scribe.Level.Trace"
      case "debug" => q"_root_.scribe.Level.Debug"
      case "info" => q"_root_.scribe.Level.Info"
      case "warn" => q"_root_.scribe.Level.Warn"
      case "error" => q"_root_.scribe.Level.Error"
    }
  }

  def autoLevel[M](c: blackbox.Context)(message: c.Tree)(loggable: c.Expr[Loggable[M]])
                  (implicit m: c.WeakTypeTag[M]): c.Tree = {
    import c.universe._

    val level = getLevel(c)
    log(c)(level, message, reify[Option[Throwable]](None))(loggable)
  }

  def autoLevel2[M](c: blackbox.Context)(message: c.Tree, t: c.Expr[Throwable])(loggable: c.Expr[Loggable[M]])
                   (implicit m: c.WeakTypeTag[M]): c.Tree = {
    import c.universe._

    val level = getLevel(c)
    log(c)(level, message, c.Expr[Option[Throwable]](q"Option($t)"))(loggable)
  }

  def log[M](c: blackbox.Context)
            (level: c.Tree,
             message: c.Tree,
             throwable: c.Expr[Option[Throwable]])
            (loggable: c.Expr[Loggable[M]])
            (implicit m: c.WeakTypeTag[M]): c.Tree = {
    import c.universe._

    val logger = c.prefix.tree
    val EnclosingType(className, methodName) = enclosingType(c)
    val line = c.enclosingPosition.line match {
      case -1 => None
      case n => Some(n)
    }
    val fileName = c.enclosingPosition.source.path
    val dcn = if (logger.tpe =:= typeOf[Logger]) {
      q"$logger.overrideClassName.getOrElse($className)"
    } else {
      q"$className"
    }
    val dmn = if (logger.tpe =:= typeOf[Logger]) {
      q"if ($logger.overrideClassName.nonEmpty) None else $methodName"
    } else {
      q"$methodName"
    }
    val dln = if (logger.tpe =:= typeOf[Logger]) {
      q"if ($logger.overrideClassName.nonEmpty) None else $line"
    } else {
      q"$line"
    }
    val messageFunction = c.typecheck(q"() => $message")
    c.internal.changeOwner(message, c.internal.enclosingOwner, messageFunction.symbol)
    q"$logger.log(_root_.scribe.LogRecord[$m]($level, $level.value, $messageFunction, $loggable, $throwable, $fileName, $dcn, $dmn, $dln))"
  }

  def executionContext(c: blackbox.Context): c.Expr[ExecutionContext] = {
    import c.universe._

    implicit val liftablePosition: c.universe.Liftable[scribe.Position] = new Liftable[scribe.Position] {
      override def apply(p: scribe.Position): c.universe.Tree = q"scribe.Position(${p.className}, ${p.methodName}, ${p.line}, ${p.column}, ${p.fileName})"
    }

    Position.push(position(c))
    val stack = Position.stack
    try {
      c.Expr[ExecutionContext](
        q"""
          new scribe.LoggingExecutionContext(scala.concurrent.ExecutionContext.global, List(..$stack))
       """)
    } finally {
      Position.pop()
    }
  }

  def pushPosition(c: blackbox.Context)(): c.Expr[Unit] = {
    import c.universe._

    val p = position(c)
    Position.push(p)
    reify(())
  }

  def position(c: blackbox.Context): Position = {
    val EnclosingType(className, methodName) = enclosingType(c)
    val line = c.enclosingPosition.line match {
      case -1 => None
      case n => Some(n)
    }
    val column = c.enclosingPosition.column match {
      case -1 => None
      case n => Some(n)
    }
    val fileName = c.enclosingPosition.source.path
    Position(className, methodName, line, column, fileName)
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