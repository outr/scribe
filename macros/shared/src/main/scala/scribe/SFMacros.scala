package scribe

import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.time.{Instant, ZoneId}
import java.util.Locale

import scala.annotation.compileTimeOnly
import scala.collection.mutable.ListBuffer
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

@compileTimeOnly("Enable macros to expand")
object SFMacros {
  def sf(c: blackbox.Context)(args: c.Tree*): c.Tree = {
    import c.universe._

    sealed trait SFBlock

    object SFBlock {
      case class RawString(s: String) extends SFBlock
      case class Formatting(fs: FormatSupport) extends SFBlock
      case class Variable(v: c.Tree) extends SFBlock
    }

    abstract class FormatSupport(val block: String) {
      def format(v: c.Tree): c.Tree
    }

    def exactly(value: String, fsf: String => FormatSupport): String => Option[FormatSupport] = (s: String) => if (value == s) {
      Some(fsf(s))
    } else {
      None
    }

    def regex(regex: String, fsf: String => FormatSupport): String => Option[FormatSupport] = (s: String) => if (s.matches(regex)) {
      Some(fsf(s))
    } else {
      None
    }

    def chrono(matcher: String,
               f: String,
               field: c.Expr[ChronoField],
               mod: c.Expr[Int => Any] = reify((i: Int) => i)): String => Option[FormatSupport] = {
      (s: String) => if (s == matcher) {
        Some(new FormatSupport(s) {
          override def format(v: c.Tree): c.Tree = if (f.nonEmpty) {
            decFormat(q"$mod(date($v).get($field))", f)
          } else {
            q"$mod(date($v).get($field)).toString"
          }
        })
      } else {
        None
      }
    }

    class DecimalFormatSupport(block: String) extends FormatSupport(block) {
      override def format(v: c.Tree): c.Tree = decFormat(v, block)
    }

    def decFormat(v: c.Tree, pattern: String): c.Tree = q"dec($v, $pattern)"

    val formatters = ListBuffer.empty[String => Option[FormatSupport]]

    // Time
    formatters += chrono("tH", "00", reify(ChronoField.CLOCK_HOUR_OF_DAY))
    formatters += chrono("tI", "00", reify(ChronoField.CLOCK_HOUR_OF_AMPM))
    formatters += chrono("tk", "0", reify(ChronoField.HOUR_OF_DAY))
    formatters += chrono("tl", "0", reify(ChronoField.HOUR_OF_AMPM))
    formatters += chrono("tM", "00", reify(ChronoField.MINUTE_OF_HOUR))
    formatters += chrono("tS", "00", reify(ChronoField.SECOND_OF_MINUTE))
    formatters += chrono("tL", "00", reify(ChronoField.MILLI_OF_SECOND))
    formatters += chrono("tN", "00", reify(ChronoField.NANO_OF_SECOND))
    formatters += chrono("tp", "", reify(ChronoField.AMPM_OF_DAY), reify((value: Int) => if (value == 0) "am" else "pm"))
    formatters += exactly("tz", new FormatSupport(_) {
                    override def format(v: c.Tree): c.Tree = reify(ZoneId.systemDefault().getRules.getOffset(Instant.now()).toString.replaceAllLiterally(":", "")).tree //q"date($v)" decFormat(q"date($v).getDayOfMonth()", "00")
                  })
    formatters += exactly("tZ", new FormatSupport(_) {
                    override def format(v: c.Tree): c.Tree = reify(ZoneId.systemDefault().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault)).tree //q"date($v)" decFormat(q"date($v).getDayOfMonth()", "00")
                  })
    formatters += chrono("ts", "00", reify(ChronoField.INSTANT_SECONDS))

    // Date
    formatters += exactly("tB", new FormatSupport(_) {
                    override def format(v: c.Tree): c.Tree = q"date($v).getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault)"
                  })
    formatters += exactly("tb", new FormatSupport(_) {
                    override def format(v: c.Tree): c.Tree = q"date($v).getMonth().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault)"
                  })
    formatters += exactly("th", new FormatSupport(_) {
                    override def format(v: c.Tree): c.Tree = q"date($v).getMonth().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault)"
                  })
    formatters += exactly("tA", new FormatSupport(_) {
                    override def format(v: c.Tree): c.Tree = q"date($v).getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault)"
                  })
    formatters += exactly("ta", new FormatSupport(_) {
                    override def format(v: c.Tree): c.Tree = q"date($v).getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault)"
                  })
    formatters += chrono("tC", "00", reify(ChronoField.YEAR_OF_ERA))
    formatters += chrono("tY", "0000", reify(ChronoField.YEAR))
    formatters += chrono("ty", "00", reify(ChronoField.YEAR), reify((value: Int) => value % 100))
    formatters += chrono("tj", "000", reify(ChronoField.DAY_OF_YEAR))
    formatters += chrono("tm", "00", reify(ChronoField.MONTH_OF_YEAR))
    formatters += chrono("td", "00", reify(ChronoField.DAY_OF_MONTH))
    formatters += chrono("te", "0", reify(ChronoField.DAY_OF_MONTH))
    formatters += exactly("tR", new MultiFormatSupport(_, "tH", ":", "tM"))
    formatters += exactly("tT", new MultiFormatSupport(_, "tH", ":", "tM", ":", "tS"))
    formatters += exactly("tr", new MultiFormatSupport(_, "tI", ":", "tM", ":", "tS", " ", "Tp"))
    formatters += exactly("td", new MultiFormatSupport(_, "tm", "/", "td", "/", "ty"))
    formatters += exactly("tF", new MultiFormatSupport(_, "tY", "-", "tm", "-", "td"))
    formatters += exactly("tc", new MultiFormatSupport(_, "ta", " ", "tb", " ", "td", " ", "tT", " ", "tZ", " ", "tY"))
    formatters += ((s: String) => {
      if (s.startsWith("T")) {
        val lowered = s.charAt(0).toLower + s.substring(1)
        formatters.toStream.flatMap(_(lowered)).headOption.map { underlying =>
          new FormatSupport(s) {
            override def format(v: c.Tree): c.Tree = {
              val result = underlying.format(v)
              q"$result.toUpperCase()"
            }
          }
        }
      } else {
        None
      }
    })

    // Numeric
    formatters += regex("[0#,.]+", new DecimalFormatSupport(_))

    class MultiFormatSupport(block: String, subBlocks: String*) extends FormatSupport(block) {
      override def format(v: c.Tree): c.Tree = {
        var tree: c.Tree = null
        subBlocks.foreach { sb =>
          val s = formatters.toStream.flatMap(_(sb)).headOption match {
            case Some(fs) => fs.format(v)
            case None => q"$sb.toString"
          }
          if (tree == null) {
            tree = s
          } else {
            tree = q"$tree.concat($s)"
          }
        }
        tree
      }
    }

    c.prefix.tree match {
      case Apply(_, List(Apply(_, rawParts))) => {
        val parts = rawParts map { case t @ Literal(Constant(const: String)) => (const, t.pos) }
        val list = ListBuffer.empty[SFBlock]
        parts.zipWithIndex.foreach {
          case ((raw, _), index) => {
            if (raw.nonEmpty) {
              val close = raw.indexOf('}')
              if (raw.startsWith("{") && close > 1) {
                val block = raw.substring(1, close)
                formatters.toStream.flatMap(_(block)).headOption match {
                  case Some(fs) => {
                    list += SFBlock.Formatting(fs)

                    val extra = raw.substring(close + 1)
                    if (extra.nonEmpty) {
                      list += SFBlock.RawString(extra)
                    }
                  }
                  case None => list += SFBlock.RawString(raw)
                }
              } else {
                list += SFBlock.RawString(raw)
              }
            }
            if (index < args.length) list += SFBlock.Variable(args(index))
          }
        }
        var lastVariable: Option[SFBlock.Variable] = None
        var o: c.Tree = null
        def out(v: c.Tree): Unit = if (o == null) {
          o = v
        } else {
          o = q"$o.concat($v)"
        }

        def outputLastVariable(): Unit = lastVariable.foreach { v =>
          out(q"String.valueOf(${v.v})")
          lastVariable = None
        }

        list.foreach {
          case b: SFBlock.RawString => {
            outputLastVariable()
            out(q"${b.s}")
          }
          case b: SFBlock.Formatting => {
            out(b.fs.format(lastVariable.get.v))
            lastVariable = None
          }
          case b: SFBlock.Variable => {
            lastVariable = Some(b)
          }
        }
        outputLastVariable()
        q"""
            import scribe.SFInterpolator._

            transaction {
              $o
            }
         """
      }
    }
  }
}
