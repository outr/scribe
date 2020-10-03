package scribe

import scala.quoted._
import scala.tasty.Tasty

object FormatterInterpolator {
  def apply(ctx: Expr[StringContext], args: Expr[Seq[Any]])(implicit tasty: Tasty): Expr[Formatter]
}