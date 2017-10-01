package acolyte.reactivemongo

import scala.concurrent.{ Future, Promise }

import org.specs2.concurrent.ExecutionEnv

class ScopeSpec(implicit ee: ExecutionEnv)
    extends org.specs2.mutable.Specification {

  "Scope" title

  "Factory" should {
    "be async" in {
      resolve[Unit, Future[String], Future[String]] must beAnInstanceOf[ScopeFactory.AsyncScopeFactory[Unit, String]]
    }

    "be default" in {
      resolve[Unit, String, Future[String]] must beAnInstanceOf[ScopeFactory.DefaultScopeFactory[Unit, String]]
    }
  }

  "Scope" should {
    "be default" in {
      val opened = Promise[Unit]()
      val closed = Promise[Unit]()

      def withStrScope(f: Unit ⇒ String): Future[String] = withScope(
        open = { () ⇒ opened.success({}).future },
        close = { _: Unit ⇒ closed.success({}); () }
      )(f)

      withStrScope { _ ⇒ "done" } must beEqualTo("done").await and {
        opened.future must beEqualTo({}).await
      } and {
        closed.future must beEqualTo({}).await
      }
    }

    "be async" in {
      val opened = Promise[Unit]()
      val closed = Promise[Unit]()

      def withFutureScope(f: Unit ⇒ Future[Int]): Future[Int] =
        withScope(
          open = { () ⇒ opened.success({}).future },
          close = { _: Unit ⇒ closed.success({}); () }
        )(f)

      withFutureScope { _ ⇒ Future.successful(2) } must beEqualTo(2).await and {
        opened.future must beEqualTo({}).await
      } and {
        closed.future must beEqualTo({}).await
      }
    }
  }

  // ---

  def resolve[Resource, InnerResult, OuterResult <: Future[_]](implicit sf: ScopeFactory.Aux[Resource, InnerResult, OuterResult]) = sf

  def withScope[InnerResult](open: () ⇒ Future[Unit], close: Unit ⇒ Unit)(f: Unit ⇒ InnerResult)(implicit sf: ScopeFactory[Unit, InnerResult]): sf.OuterResult = sf(open, close)(f)
}
