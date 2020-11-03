package scribe

import scala.annotation.compileTimeOnly
import scala.concurrent.ExecutionContext
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

@compileTimeOnly("Enable macros to expand")
object Macros {
  def executionContext(c: blackbox.Context): c.Expr[ExecutionContext] = {
    import c.universe._

    executionContextCustom(c)(c.Expr[ExecutionContext](q"_root_.scala.concurrent.ExecutionContext.global"))
  }

  def executionContextCustom(c: blackbox.Context)(context: c.Expr[ExecutionContext]): c.Expr[ExecutionContext] = {
    import c.universe._

    implicit val liftablePosition: c.universe.Liftable[scribe.Position] = new Liftable[scribe.Position] {
      override def apply(p: scribe.Position): c.universe.Tree = q"scribe.Position(${p.className}, ${p.methodName}, ${p.line}, ${p.column}, ${p.fileName})"
    }

    Position.push(position(c))
    val stack = Position.stack
    try {
      c.Expr[ExecutionContext](
        q"""
          new scribe.LoggingExecutionContext($context, List(..$stack))
       """)
    } finally {
      Position.pop()
    }
  }

  def async[Return](c: blackbox.Context)(f: c.Tree)(implicit r: c.WeakTypeTag[Return]): c.Tree = {
    import c.universe._

    val function = c.typecheck(q"() => $f")
    c.internal.changeOwner(f, c.internal.enclosingOwner, function.symbol)
    q"""
       try {
         $function() match {
           case future: Future[_] => future.recover({
             case throwable: Throwable => throw scribe.Position.fix(throwable)
           })(scribe.Execution.global).asInstanceOf[$r]
           case result => result
         }
       } catch {
         case t: Throwable => throw scribe.Position.fix(t)
       }
     """
  }

  def future[Return](c: blackbox.Context)(f: c.Tree): c.Tree = {
    import c.universe._

    val context = executionContext(c)
    val function = c.typecheck(q"() => $f")
    c.internal.changeOwner(f, c.internal.enclosingOwner, function.symbol)
    q"""
       import _root_.scala.concurrent.Future
       implicit def executionContext: _root_.scala.concurrent.ExecutionContext = $context

       val future = Future($function())(executionContext)
       future.recover({
         case throwable: Throwable => throw scribe.Position.fix(throwable)
       })(executionContext)
     """
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