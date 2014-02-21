package acolyte

import scala.tools.nsc.Global
import scala.tools.nsc.plugins.{ Plugin, PluginComponent }
import scala.tools.nsc.transform.Transform

class MatchPlugin(val global: Global) extends Plugin {
  val name = "rich-match"
  val description = "Rich or refactored pattern matching"
  val components = List[PluginComponent](MatchComponent)

  private object MatchComponent extends PluginComponent with Transform {
    val global: MatchPlugin.this.global.type = MatchPlugin.this.global
    override val runsRightAfter = Some("parser")
    override val runsAfter = runsRightAfter.toList
    override val runsBefore = List[String]("typer")
    val phaseName = "rich-patmat"

    def newTransformer(unit: global.CompilationUnit) = MatchTransformer

    object MatchTransformer extends global.Transformer {
      import scala.collection.mutable.ListBuffer
      import global.{
        abort,
        reporter,
        Block,
        CaseDef,
        Match,
        Position,
        Tree,
        ValDef
      }

      override def transform(tree: Tree): Tree = tree match {
        case m @ Match(_, _) ⇒ refactorMatch(m)
        case _               ⇒ super.transform(tree)
      }

      val tildeTerm = global.newTermName("$tilde")

      @inline private def refactorMatch(orig: Match): Tree =
        orig match {
          case Match(t, cs) ⇒ {
            import global.{
              Apply,
              Bind,
              Block,
              Constant,
              Ident,
              Literal
            }

            val vds = ListBuffer[ValDef]()
            val cds = cs.map {
              case ocd @ CaseDef(Apply(Ident(it), xt), g, by) if (
                it == tildeTerm) ⇒
                (xt.headOption, xt.tail) match {
                  case (Some(xt @ Apply(ex, xa)), Apply(_, ua) :: Nil) ⇒
                    val (vd, cd) = caseDef(vds.size, ocd.pos, xt.pos, ex, xa,
                      ua, g, by)
                    vds += vd
                    cd

                  case (Some(xt @ Apply(ex, xa)), Bind(uf, ua) :: Nil) ⇒
                    val (vd, cd) = caseDef(vds.size, ocd.pos, xt.pos, ex, xa,
                      List(Bind(uf, ua)), g, by)
                    vds += vd
                    cd

                  case (Some(xt @ Apply(ex, xa)), Ident(i) :: Nil) ⇒
                    val (vd, cd) = caseDef(vds.size, ocd.pos, xt.pos, ex, xa,
                      List(Ident(i)), g, by)
                    vds += vd
                    cd

                  /*
                   case (Some(xt @ Apply(ex, xa)),
                   Literal(Constant(())) :: Nil) ⇒
                   val (vd, cd) = caseDef(vds.size, ocd.pos, xt.pos, ex, xa,
                   List(Literal(Constant(()))), g, by)
                   vds += vd
                   cd
                   */

                  case _ ⇒
                    reporter.error(ocd.pos, "Invalid ~ pattern")
                    abort("Invalid ~ pattern")

                }

              case cd ⇒ cd
            }

            if (vds.isEmpty) orig // revert to original Match
            else Block(vds.toList, Match(t, cds).setPos(orig.pos))
          }
          case _ ⇒
            reporter.error(orig.pos, "Invalid Match")
            abort("Invalid Match")
        }

      @inline private def caseDef[T](i: Int, cp: Position, xp: Position, ex: Tree, xa: List[Tree], ua: List[Tree], g: Tree, b: Tree): (ValDef, CaseDef) = {

        import scala.reflect.io.VirtualFile
        import scala.reflect.internal.util.BatchSourceFile
        import global.{
          atPos,
          show,
          Apply,
          Ident,
          Literal,
          Modifiers,
          TypeTree
        }

        val of = xp.source.file
        val file = new VirtualFile(of.name, of.path + "#refactored-match-" + i)

        // ValDef
        val xn = global.treeBuilder.freshTermName("Xtr")
        val vd = ValDef(Modifiers(), xn, TypeTree(), Apply(ex, xa))
        val vdc =
          s"${show(vd)} // generated from ln ${xp.line}, col ${xp.column - 1}"

        val vdp = xp.withPoint(0).withSource(new BatchSourceFile(file, vdc), 0)

        /*
[error] /path/to/file.scala#refactored-match-M:1: Compilation error.
[error] Error details.
[error] val Xtr1 = B() // generated from ln L, col C
         */

        // CaseDef
        /*
        val pat = ua match {
          case Literal(Constant(())) :: Nil ⇒ Apply(Ident(xn), Nil)
          case _                            ⇒ Apply(Ident(xn), ua)
        }
         */
        val pat = Apply(Ident(xn), ua)
        val cd = CaseDef(pat, g, b)
        val cdc =
          s"${show(cd)} // generated from ln ${cp.line}, col ${cp.column - 5}"
        val cdp = cp.withPoint(0).withSource(new BatchSourceFile(file, cdc), 0)

        /*
[error] /path/to/file.scala#refactored-match-M:1: value Xtr0 is not a case class constructor, nor does it have an unapply/unapplySeq method
[error] case Xtr1((a @ _)) => Nil // generated from ln L, col C
         */

        (atPos(vdp)(vd), atPos(cdp)(cd))
      }
    }
  }
}
