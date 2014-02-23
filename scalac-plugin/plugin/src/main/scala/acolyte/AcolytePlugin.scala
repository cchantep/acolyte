package acolyte

import scala.tools.nsc.Global
import scala.tools.nsc.plugins.{ Plugin, PluginComponent }
import scala.tools.nsc.transform.Transform

class AcolytePlugin(val global: Global) extends Plugin {
  val name = "acolyte"
  val description = "Syntax extensions: rich pattern matching"
  val components = List[PluginComponent](MatchComponent)

  var debug: Boolean with NotNull = false

  override def processOptions(options: List[String], error: String ⇒ Unit) {
    for (o ← options) {
      if (o == "debug") debug = true
    }
  }

  override val optionsHelp: Option[String] = Some(
    "  -P:acolyte:debug             Enable debug")

  private object MatchComponent extends PluginComponent with Transform {
    val global: AcolytePlugin.this.global.type = AcolytePlugin.this.global
    override val runsRightAfter = Some("parser")
    override val runsAfter = runsRightAfter.toList
    override val runsBefore = List[String]("typer")
    val phaseName = "rich-patmat"

    def newTransformer(unit: global.CompilationUnit) = MatchTransformer

    object MatchTransformer extends global.Transformer {
      import scala.collection.mutable.ListBuffer
      import global.{
        //abort,
        reporter,
        Block,
        CaseDef,
        Match,
        Position,
        Tree,
        ValDef
      }

      override def transform(tree: Tree): Tree = tree match {
        case m @ Match(_, _) ⇒ {
          val richMatch = refactorMatch(m)

          if (debug) reporter.info(m.pos,
            s"Rich Match refactored: ${global show richMatch}", true)

          richMatch
        }
        case _ ⇒ super.transform(tree)
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
              case ocd @ CaseDef(Apply(Ident(it), x), g, by) if (
                it == tildeTerm) ⇒
                (x.headOption, x.tail) match {
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

                  case (Some(xt @ Apply(ex, xa)), Nil) ⇒
                    // no binding
                    val (vd, cd) = caseDef(vds.size, ocd.pos, xt.pos, ex, xa,
                      Nil, g, by)
                    vds += vd
                    cd

                  case _ ⇒
                    reporter.error(ocd.pos, "Invalid ~ pattern")
                    //abort("Invalid ~ pattern"
                    ocd
                }

              case cd ⇒ cd
            }

            if (vds.isEmpty) orig // revert to original Match
            else Block(vds.toList, Match(t, cds).setPos(orig.pos))
          }
          case _ ⇒
            reporter.error(orig.pos, "Invalid Match")
            //abort("Invalid Match")
            orig
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

        // CaseDef
        val pat = Apply(Ident(xn), ua)
        val cd = CaseDef(pat, g, b)
        val cdc =
          s"${show(cd)} // generated from ln ${cp.line}, col ${cp.column - 5}"
        val cdp = cp.withPoint(0).withSource(new BatchSourceFile(file, cdc), 0)

        (atPos(vdp)(vd), atPos(cdp)(cd))
      }
    }
  }
}
