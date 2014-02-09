package acolyte

/** Utility macros. */
package object macros {
  import scala.collection.mutable.ListBuffer
  import scala.reflect.macros.Context
  import scala.language.experimental.macros

  /**
   * Value/literal wrapper required to refactor pattern match.
   * Removed by rmatch macro. DO NOT USE DIRECTLY.
   */
  case class Xt(extractor: Any, unapply: Any = null)
  private val XtClassName = "acolyte.macros.Xt"

  /**
   * Macro for rich/refactored pattern matching.
   *
   * {{{
   * import acolyte.macros.{ Xt, rmatch }
   *
   * val res = rmatch {
   *   Xt(v) match {
   *     case Xt(ExtractorWithParam("b"), _) => // ...
   *   }
   * }
   * }}}
   */
  def rmatch[T](b: T): T = macro refactorMatch[T]

  /**
   * Extractor factory based on regular expression.
   *
   * {{{
   * import acolyte.macros.{ Xt, Regex, rmatch }
   *
   * val res: Boolean = rmatch {
   *   Xt("abc") match {
   *     case Xt(Regex("(.+)b([a-z]+)$"), (x, y)) => true // x == "a", y == "b"
   *     case _ => false
   *   }
   * }
   * }}}
   */
  case class Regex(e: String) {
    lazy val re = e.r

    /** See [[scala.util.matching.Regex.unapplySeq]]. */
    def unapplySeq(target: Any): Option[List[String]] = re.unapplySeq(target)
  }

  // ---

  def refactorMatch[T](c: Context)(b: c.Expr[T])(implicit tt: c.WeakTypeTag[T]): c.Expr[T] = {
    import c.universe.{
      Apply,
      Block,
      Match,
      Transformer,
      Select,
      TermName,
      Tree,
      ValDef
    }

    var vds: List[ValDef] = null

    val tb = new Transformer {
      override def transform(tree: Tree): Tree = tree match {
        case m @ Match(Apply(Select(_, _), List(xa,
          Select(Select(p, s), _))), cds) if (
          p.toString.startsWith("acolyte.macros.") && s.toString == "Xt") ⇒

          val (vs, cs) = caseDefs(c)(b, cds)
          vds = vs

          c.resetAllAttrs(Match(xa, cs))

        case x ⇒
          val msg = s"Match expected"
          c.error(x.pos, msg)
          sys.error(msg)
      }

    } transform b.tree

    if (vds.size > 0) {
      try {
        val rb = c.typeCheck(Block(vds :+ tb: _*))

        // TODO: Add debug option, log refactored block if enabled
        println(s"rmatch:\r\n${c.universe.show(rb)}")

        c.Expr[T](rb)
      } catch {
        case t: Throwable ⇒
          val msg = s"Invalid refactored match"
          c.error(b.tree.pos, s"${msg}: ${t.getMessage}")
          sys.error(msg)

      }
    } else b
  }

  @inline private def caseDefs[T](c: Context)(b: c.Expr[T], cs: List[c.universe.CaseDef]): (List[c.universe.ValDef], List[c.universe.CaseDef]) = {
    import c.universe.{
      Apply,
      Bind,
      Block,
      CaseDef,
      Constant,
      Ident,
      Literal,
      Match,
      TypeTree,
      Transformer,
      Tree,
      UnApply,
      ValDef,
      showRaw
    }

    val vds = ListBuffer[ValDef]()
    val cds = cs.map {
      case m @ CaseDef(Apply(x, xt), g, by) if (showRaw(x).
        contains(XtClassName)) ⇒ (xt.headOption, xt.tail) match {
        case (Some(xt @ Apply(ex: TypeTree, xa)), Apply(_, ua) :: Nil) ⇒
          val (vd, cd) = caseDef(c)(vds.size, xt.pos, ex.original, xa,
            ua, g, by)
          vds += vd
          cd

        case (Some(xt @ Apply(ex: TypeTree, xa)), Bind(uf, ua) :: Nil) ⇒
          val (vd, cd) = caseDef(c)(vds.size, xt.pos, ex.original, xa,
            List(Bind(uf, ua)), g, by)
          vds += vd
          cd

        case (Some(xt @ Apply(ex: TypeTree, xa)), Ident(i) :: Nil) ⇒
          val (vd, cd) = caseDef(c)(vds.size, xt.pos, ex.original, xa,
            List(Ident(i)), g, by)
          vds += vd
          cd

        case (Some(xt @ Apply(ex: TypeTree, xa)),
          Literal(Constant(())) :: Nil) ⇒
          val (vd, cd) = caseDef(c)(vds.size, xt.pos, ex.original, xa,
            List(Literal(Constant(()))), g, by)
          vds += vd
          cd

        case _ ⇒
          c.error(m.pos, "Invalid Xt pattern")
          sys.error("Invalid Xt pattern")

      }

      case cd @ CaseDef(x, g, by) ⇒
        println(s"==> ${showRaw(cd)}")
        cd

    }

    (vds.toList, cds)
  }

  @inline private def caseDef[T](c: Context)(i: Int, xp: c.universe.Position, ex: c.universe.Tree, xa: List[c.universe.Tree], ua: List[c.universe.Tree], g: c.universe.Tree, b: c.universe.Tree): (c.universe.ValDef, c.universe.CaseDef) = {

    import c.universe.{
      Apply,
      CaseDef,
      Constant,
      Ident,
      Literal,
      Match,
      Modifiers,
      ValDef,
      Tree,
      TypeTree,
      newTermName
    }

    val xn = newTermName(c.fresh("Xtr"))
    val xt = c.resetAllAttrs(Ident(ex.tpe.typeSymbol))
    val vd = ValDef(Modifiers(), xn, xt, Apply(ex, xa))

    try {
      c.typeCheck(vd)
    } catch {
      case t: Throwable ⇒
        val msg = s"Invalid extractor"
        c.error(xp, s"${msg}: ${t.getMessage}")
        sys.error(msg)
    }

    val pat = ua match {
      case Literal(Constant(())) :: Nil ⇒ Apply(Ident(xn), Nil)
      case _                            ⇒ Apply(Ident(xn), ua)
    }
    val cd = CaseDef(pat, g, b)

    // TODO: Typecheck cd

    (vd, cd)
  }

}
