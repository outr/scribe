package com.outr.scribe

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

object Macros {
  def enclosingMethod(c: whitebox.Context): c.Expr[Option[String]] = {
    import c.universe._
    c.enclosingMethod match {
      case DefDef(_, name, _, _, _, _) =>
        c.universe.reify(Some(c.literal(name.toString).splice))
      case _ => c.universe.reify(None)
    }
  }

  def log(c: whitebox.Context)(level: c.Expr[Level], message: c.Tree): c.universe.Tree = {
    import c.universe._
    val logger = c.prefix.tree
    val method = enclosingMethod(c)
    val line = c.enclosingPosition.line
    q"$logger.log($level, $message, $method, Some($line))"
  }

  def trace(c: whitebox.Context)(message: c.Tree): c.universe.Tree =
    log(c)(c.universe.reify(Level.Trace), message)

  def debug(c: whitebox.Context)(message: c.Tree): c.universe.Tree =
    log(c)(c.universe.reify(Level.Debug), message)

  def info(c: whitebox.Context)(message: c.Tree): c.universe.Tree =
    log(c)(c.universe.reify(Level.Info), message)

  def warn(c: whitebox.Context)(message: c.Tree): c.universe.Tree =
    log(c)(c.universe.reify(Level.Warn), message)

  def error(c: whitebox.Context)(message: c.Tree): c.universe.Tree =
    log(c)(c.universe.reify(Level.Error), message)
}
