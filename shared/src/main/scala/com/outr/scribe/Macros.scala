package com.outr.scribe

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

object Macros {
  def enclosingMethod(c: whitebox.Context): c.universe.Expr[Option[String]] = {
    import c.universe._
    val term = c.internal.enclosingOwner.asTerm
    val name = term.name.decodedName.toString
    if (name.head == '<') {
      reify(None)
    } else {
      c.Expr(q"Some($name)")
    }
  }

  def log(c: whitebox.Context)(level: c.Expr[Level], message: c.Tree): c.universe.Tree = {
    import c.universe._
    val logger = c.prefix.tree
    val method = enclosingMethod(c)
    val line = c.enclosingPosition.line
    q"$logger.log($level, $message, $method, $line)"
  }

  def trace(c: whitebox.Context)(message: c.Tree): c.universe.Tree = log(c)(c.universe.reify(Level.Trace), message)

  def debug(c: whitebox.Context)(message: c.Tree): c.universe.Tree = log(c)(c.universe.reify(Level.Debug), message)

  def info(c: whitebox.Context)(message: c.Tree): c.universe.Tree = log(c)(c.universe.reify(Level.Info), message)

  def warn(c: whitebox.Context)(message: c.Tree): c.universe.Tree = log(c)(c.universe.reify(Level.Warn), message)

  def error(c: whitebox.Context)(message: c.Tree): c.universe.Tree = log(c)(c.universe.reify(Level.Error), message)
}
