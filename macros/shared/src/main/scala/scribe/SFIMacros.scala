package scribe

import scala.annotation.compileTimeOnly
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@compileTimeOnly("Enable macros to expand")
object SFIMacros {
  def sfiImpl(c: whitebox.Context)(args: c.Expr[Any]*): c.Expr[String] = {
    import c.universe._

    val constantParts = extractConstantPartsAndTreatEscapes(c)

    if (args.isEmpty) {
      return c.Expr(Literal(Constant(constantParts.mkString(""))))
    }

    var initialLength = constantParts.map(_.length).sum

    val (valDeclarations, lenNames, arguments: Seq[c.universe.Tree]) = args.zipWithIndex.map {
      case (e, index) =>
        val name = TermName("__local" + index)
        e.actualType match {
          case tt if tt.typeSymbol.asClass.isPrimitive =>
            // A kind of optimization to not calculate primitive length in advance, let the StringBuilder
            // to deal with primitive toString (it's better than i.e. Int.toString static method).
            initialLength += 9
            (Nil, Nil, e.tree)
          case tt if tt <:< typeOf[CharSequence] =>
            val expr =
              q"""
                val $name = {
                  val tmp = $e
                  if (tmp eq null) "null" else tmp
               }"""
            (List(expr), List(name), Ident(name))
          case _ =>
            val expr =
              q"""
                val $name = {
                  val tmp = $e
                  if (tmp eq null) "null" else tmp.toString
               }"""
            (List(expr), List(name), Ident(name))
        }
    }.unzip3

    val allParts = getAllPartsForAppend(c)(constantParts, arguments)

    // code generation

    val plusLenExpr = lengthSum(c)(initialLength, lenNames.flatten.toList)

    val stringBuilderWithAppends = newStringBuilderWithAppends(c)(q"new java.lang.StringBuilder(len)", allParts.reverse)

    val stats = valDeclarations.flatten.toList ++
      List(q"val len = $plusLenExpr") :+
      q"$stringBuilderWithAppends.toString"

    c.Expr(
      q"..$stats"
    )
  }

  private def getAllPartsForAppend(c: whitebox.Context)(rawParts: Seq[String], arguments: Seq[c.universe.Tree]) = {
    import c.universe._

    def nilOrConst(s: String) = {
      if (s.isEmpty) Nil else List(Literal(Constant(s)))
    }

    rawParts.zipAll(arguments, null, null).flatMap {
      case (raw, null) => nilOrConst(raw)
      case (raw, arg) if raw != null => nilOrConst(raw) :+ arg
    }.toList
  }

  private def extractConstantPartsAndTreatEscapes(c: whitebox.Context): List[String] = {
    import c.universe._

    val rawParts = c.prefix.tree match {
      case Apply(_, List(Apply(_, list))) => list
    }

    rawParts.map {
      case Literal(Constant(rawPart: String)) => StringContext.treatEscapes(rawPart)
    }
  }

  private def newStringBuilderWithAppends(c: whitebox.Context)
                                         (newStringBuilderExpr: c.universe.Tree,
                                          expressions: List[c.universe.Tree]): c.universe.Tree = {
    import c.universe._

    if (expressions.isEmpty) {
      newStringBuilderExpr
    } else {
      Apply(Select(newStringBuilderWithAppends(c)(newStringBuilderExpr, expressions.tail), TermName("append")), List(expressions.head))
    }
  }

  private def lengthSum(c: whitebox.Context)(const: Int, names: List[c.universe.TermName]): c.universe.Tree = {
    import c.universe._

    if (names.isEmpty) {
      Literal(Constant(const))
    } else {
      Apply(Select(Select(Ident(names.head), TermName("length")), TermName("$plus")), List(lengthSum(c)(const, names.tail)))
    }
  }
}
