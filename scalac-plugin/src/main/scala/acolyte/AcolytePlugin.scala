package acolyte

import scala.tools.nsc.Global
import scala.tools.nsc.plugins.{ Plugin, PluginComponent }
import scala.tools.nsc.transform.Transform

import scala.reflect.internal.util.BatchSourceFile

class AcolytePlugin(val global: Global) extends Plugin {
  val name = "acolyte"
  val description = "Syntax extensions: Extractors with arguments"
  val components = List[PluginComponent](ExtractorComponent)

  var debug: Boolean = false

  override def processOptions(options: List[String], error: String ⇒ Unit) {
    for (o ← options) {
      if (o == "debug") debug = true
    }
  }

  override val optionsHelp: Option[String] = Some(
    "  -P:acolyte:debug             Enable debug")

  @inline private def withSource(pos: global.Position)(f: BatchSourceFile, shift: Int) = CompilerUtility.withSource(global)(pos, f, shift)

  private object ExtractorComponent extends PluginComponent with Transform {
    val global: AcolytePlugin.this.global.type = AcolytePlugin.this.global
    override val runsRightAfter = Some("parser")
    override val runsAfter = runsRightAfter.toList
    override val runsBefore = List[String]("typer")
    val phaseName = "rich-patmat"

    def newTransformer(unit: global.CompilationUnit) =
      new MatchTransformer(unit)

    class MatchTransformer(
        unit: global.CompilationUnit) extends global.Transformer {

      import scala.collection.mutable.ListBuffer
      import global.{
        //abort,
        reporter,
        Apply,
        Block,
        Bind,
        CaseDef,
        Constant,
        DefDef,
        Ident,
        Literal,
        Match,
        Position,
        Select,
        Tree,
        ValDef
      }
      import scala.reflect.io.VirtualFile

      override def transform(tree: Tree): Tree = refactor(tree)

      private def refactor(tree: Tree): Tree = tree match {
        case m @ Match(_, _) ⇒ {
          val richMatch = refactorMatch(m)

          if (debug) reporter.info(m.pos,
            s"Rich Match refactored: ${global show richMatch}", true)

          richMatch
        }
        case _ ⇒ super.transform(tree)
      }

      val tildeTerm = global.newTermName("$tilde")
      val scalaTerm = global.newTermName("scala")

      @inline private def refactorMatch(orig: Match): Tree = orig match {
        case Match(t, cs) ⇒ {
          val vds = ListBuffer[ValDef]()
          val tx = caseDefTransformer(vds)

          val cds = cs map {
            case ocd @ CaseDef(pat, g, by) ⇒ {
              val ocp = ocd.pos // g, by

              val of = ocp.source.file
              val file = new VirtualFile(of.name,
                s"${of.path}#refactored-match-${ocp.line}")

              val nc = CaseDef(tx.transform(pat), g, refactor(by))
              val cdc = s"${global show nc} // generated from ln ${ocp.line}, col ${ocp.column - 5}"
              val cdp = withSource(ocp.withPoint(0))(
                new BatchSourceFile(file, cdc), 0)

              global.atPos(cdp)(nc)
            }
            case cd ⇒ cd
          }

          if (vds.isEmpty) Match(t, cds)
          else Block(vds.toList, Match(t, cds).setPos(orig.pos))
        }
        case _ ⇒
          reporter.error(orig.pos, "Invalid Match")
          //abort("Invalid Match")
          orig
      }

      private def caseDefTransformer(vds: ListBuffer[ValDef]) =
        new global.Transformer {
          override def transform(tree: Tree): Tree = tree match {
            case oa @ Apply(Ident(it), x) if (it == tildeTerm) ⇒ {
              (x.headOption, x.tail) match {
                case (Some(xt @ Apply(ex, xa)), bs) ⇒ {
                  val xpo: Option[List[Tree]] = bs.headOption match {
                    case Some(Apply(Select(Ident(scalaTerm), st), ua)) if (
                      st.toString startsWith "Tuple") ⇒ Some(ua)
                    case Some(ap @ Apply(_, _))          ⇒ Some(ap :: Nil)
                    case Some(bn @ Bind(_, _))           ⇒ Some(bn :: Nil)
                    case Some(id @ Ident(_))             ⇒ Some(id :: Nil)
                    case Some(li @ Literal(Constant(_))) ⇒ Some(li :: Nil)
                    case None                            ⇒ Some(Nil)
                    case _                               ⇒ None
                  }

                  xpo.fold({
                    reporter.error(oa.pos, s"""Invalid ~ pattern: ${bs.headOption.fold("None")(global.showRaw(_))}""")
                    //abort("Invalid ~ pattern")
                    oa
                  }) { xp ⇒
                    val (vd, rp) = refactorPattern(xt.pos, ex, xa, xp)
                    vds += vd
                    rp
                  }
                }
                case _ ⇒
                  reporter.error(oa.pos, "Invalid ~ pattern")
                  //abort("Invalid ~ pattern")
                  oa

              }
            }
            case _ ⇒ super.transform(tree)
          }
        }

      @inline private def refactorPattern[T](xp: Position, ex: Tree, xa: List[Tree], ua: List[Tree]): (ValDef, Apply) = {

        import global.{ atPos, show, newTermName, Ident, Modifiers }

        val of = xp.source.file
        val file = new VirtualFile(of.name,
          s"${of.path}#refactored-match-${xp.line}")

        // ValDef
        val xn = unit.freshTermName("Xtr")
        val vd = ValDef(Modifiers(), xn, global.TypeTree(), Apply(ex, xa))
        val vdc =
          s"${show(vd)} // generated from ln ${xp.line}, col ${xp.column - 1}"

        val vdp = withSource(xp.withPoint(0))(new BatchSourceFile(file, vdc), 0)

        (atPos(vdp)(vd), Apply(Ident(xn), ua))
      }
    }
  }
}
