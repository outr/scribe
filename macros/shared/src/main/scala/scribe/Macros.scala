package scribe2

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
              list += q"scribe2.FormatBlock.RawString($raw)"
            }
            if (index > 0) {
              list += argsVector(index - 1)
            }
          }
        }
        q"scribe2.Formatter.fromBlocks(..$list)"
      }
      case _ => c.abort(c.enclosingPosition, "Bad usage of formatter interpolation.")
    }
  }
}