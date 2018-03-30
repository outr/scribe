package scribe

import scala.annotation.compileTimeOnly
import scala.collection.mutable.ListBuffer
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

@compileTimeOnly("Enable macros to expand")
object FormatterMacros {
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
              list += q"_root_.scribe.format.FormatBlock.RawString($raw)"
            }
            if (index < argsVector.size) {
              list += argsVector(index)
            }
          }
        }
        q"_root_.scribe.format.Formatter.fromBlocks(..$list)"
      }
      case _ => c.abort(c.enclosingPosition, "Bad usage of formatter interpolation.")
    }
  }
}
