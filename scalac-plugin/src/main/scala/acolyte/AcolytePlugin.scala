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
        Ident,
        Literal,
        Match,
        Name,
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

          if (debug) reporter.info(
            m.pos,
            s"Rich Match refactored: ${global show richMatch}", true)

          richMatch
        }
        case _ ⇒ super.transform(tree)
      }

      val TildeTerm = global.newTermName("$tilde")
      val ScalaTerm = global.newTermName("scala")

      @inline private def refactorMatch(orig: Match): Tree = orig match {
        case Match(t, cs) ⇒ {
          val vds = ListBuffer[ValDef]()
          val tx = caseDefTransformer(vds)

          val cds = cs map {
            case ocd @ CaseDef(pat, g, by) ⇒ {
              val ocp = ocd.pos // g, by

              val of = ocp.source.file
              val file = new VirtualFile(
                of.name,
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

      @inline private def rewriteApply(top: Position, id: Tree, x: List[Tree], vds: ListBuffer[ValDef]): Apply = (id, x.headOption) match {
        case (Ident(TildeTerm), Some(Apply(ex, xa))) ⇒ {
          val bs = x.tail

          val xpo: Option[List[Tree]] = bs.headOption match {
            case Some(Apply(Select(Ident(_), st), ua)) if (
              st.toString startsWith "Tuple") ⇒ Some(ua)

            case Some(ap @ Apply(_, _))          ⇒ Some(ap :: Nil)
            case Some(bn @ Bind(_, _))           ⇒ Some(bn :: Nil)
            case Some(id @ Ident(_))             ⇒ Some(id :: Nil)
            case Some(li @ Literal(Constant(_))) ⇒ Some(li :: Nil)
            case None                            ⇒ Some(Nil)
            case _                               ⇒ None
          }

          xpo.fold({
            reporter.error(top, s"""Invalid ~ pattern: ${bs.headOption.fold("None")(global.showRaw(_))}""")
            //abort("Invalid ~ pattern")
            Apply(id, x)
          }) { xp ⇒
            val (vd, ai) = refactorPattern(top, ex, xa)
            vds += vd
            Apply(ai, xp)
          }
        }

        case (Ident(TildeTerm), _) ⇒ {
          reporter.error(
            top,
            s"Invalid ~ pattern: application expected in bindings: $x")

          Apply(id, x)
        }

        case _ ⇒ Apply(id, x)
      }

      // Transformation states
      sealed trait TxState

      private case class AState(
          id: Tree, orig: List[Tree], refact: List[Tree]) extends TxState

      private case class BState(
          name: Name /* binding name */ ,
          orig: Option[Tree], dest: Option[Tree]) extends TxState

      private object BState {
        def apply(name: Name, target: Tree): BState =
          BState(name, Some(target), None)

      }

      @annotation.tailrec
      private def refactorApply(top: Position, cur: TxState, up: List[TxState], vds: ListBuffer[ValDef]): Apply = (cur, up) match {
        case (AState(id, ::(Apply(i, ts), as), xs), _) ⇒
          refactorApply(top, AState(i, ts, Nil),
            AState(id, as, xs) :: up, vds)

        case (AState(id, ::(Bind(n, t), as), xs), _) ⇒
          refactorApply(top, BState(n, t), AState(id, as, xs) :: up, vds)

        case (bn @ BState(_, Some(Apply(i, ts)), _), _) ⇒
          refactorApply(top, AState(i, ts, Nil), bn :: up, vds)

        case (bn @ BState(_, Some(Bind(n, t)), _), _) ⇒
          refactorApply(top, BState(n, t), bn :: up, vds)

        case (AState(id, a :: as, xs), _) ⇒
          refactorApply(top, AState(id, as, xs :+ a), up, vds)

        case (BState(an, Some(at), _), ::(BState(bn, _, _), us)) ⇒
          refactorApply(top, BState(bn, None, Some(Bind(an, at))), us, vds)

        case (BState(an, Some(at), _), ::(AState(ai, ts, xs), us)) ⇒
          refactorApply(top, AState(ai, ts, xs :+ Bind(an, at)), us, vds)

        case (BState(an, _, Some(at)), ::(BState(bn, _, _), us)) ⇒
          refactorApply(top, BState(bn, None, Some(Bind(an, at))), us, vds)

        case (BState(an, _, Some(at)), ::(AState(ai, ts, xs), us)) ⇒
          refactorApply(top, AState(ai, ts, xs :+ Bind(an, at)), us, vds)

        case (AState(id, Nil, xs), ::(AState(iu, y, z), us)) ⇒
          val ap = rewriteApply(top, id, xs, vds)
          refactorApply(top, AState(iu, y, z :+ ap), us, vds)

        case (AState(id, Nil, xs), ::(BState(un, _, _), us)) ⇒
          val ap = rewriteApply(top, id, xs, vds)
          refactorApply(top, BState(un, None, Some(ap)), us, vds)

        case (AState(id, Nil, xs), Nil) ⇒ rewriteApply(top, id, xs, vds)

        case _ ⇒
          reporter.error(top, s"Invalid ~ pattern: $cur, $up")
          global.abort(s"Invalid ~ pattern: $cur, $up")

      }

      private def caseDefTransformer(vds: ListBuffer[ValDef]) =
        new global.Transformer {
          override def transform(tree: Tree): Tree = tree match {
            case oa @ Apply(id, xa) ⇒
              refactorApply(oa.pos, AState(id, xa, Nil), Nil, vds)
            case _ ⇒ super.transform(tree)
          }
        }

      @inline private def refactorPattern[T](xp: Position, ex: Tree, xa: List[Tree]): (ValDef, Ident) = {

        import global.{ atPos, show, Ident, Modifiers }

        val of = xp.source.file
        val file = new VirtualFile(
          of.name,
          s"${of.path}#refactored-match-${xp.line}")

        // ValDef
        val xn = unit.freshTermName("Xtr")
        val vd = ValDef(Modifiers(), xn, global.TypeTree(), Apply(ex, xa))
        val vdc =
          s"${show(vd)} // generated from ln ${xp.line}, col ${xp.column - 1}"

        val vdp = withSource(xp.withPoint(0))(new BatchSourceFile(file, vdc), 0)

        atPos(vdp)(vd) → Ident(xn)
      }
    }
  }
}
