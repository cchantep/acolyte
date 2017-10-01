package acolyte.reactivemongo

import scala.concurrent.{ ExecutionContext, Future }

sealed trait Scope[Resource, InnerResult, OuterResult <: Future[_]] {
  private[reactivemongo] def apply(f: Resource ⇒ InnerResult)(implicit ec: ExecutionContext): OuterResult
}

sealed trait ScopeFactory[Resource, InnerResult] {
  type OuterResult <: Future[_]

  private[reactivemongo] def apply(
    open: () ⇒ Future[Resource],
    close: Resource ⇒ Unit
  ): Scope[Resource, InnerResult, OuterResult]
}

object ScopeFactory extends LowPriorityScopeFactory {
  type Aux[Resource, Inner, Outer <: Future[_]] = ScopeFactory[Resource, Inner] {
    type OuterResult = Outer
  }

  private class AsyncScope[Resource, Result](
      open: () ⇒ Future[Resource],
      close: Resource ⇒ Unit
  ) extends Scope[Resource, Future[Result], Future[Result]] {
    def apply(f: Resource ⇒ Future[Result])(implicit ec: ExecutionContext): Future[Result] = open().flatMap { resource ⇒
      f(resource).andThen { case _ ⇒ close(resource) }
    }
  }

  private[reactivemongo] class AsyncScopeFactory[Resource, Result]
      extends ScopeFactory[Resource, Future[Result]] {
    type OuterResult = Future[Result]

    def apply(
      open: () ⇒ Future[Resource],
      close: Resource ⇒ Unit
    ): Scope[Resource, Future[Result], Future[Result]] =
      new AsyncScope[Resource, Result](open, close)
  }

  implicit def asyncScopeFactory[Resource, Result](implicit applied: Applied.Aux[Future[Result], Future[Result]]): ScopeFactory.Aux[Resource, Future[Result], applied.Outer] = new AsyncScopeFactory[Resource, Result]
}

sealed trait LowPriorityScopeFactory { _: ScopeFactory.type ⇒
  private class DefaultScope[Resource, Result](
      open: () ⇒ Future[Resource],
      close: Resource ⇒ Unit
  ) extends Scope[Resource, Result, Future[Result]] {
    def apply(f: Resource ⇒ Result)(implicit ec: ExecutionContext): Future[Result] = open().map { resource ⇒
      try {
        f(resource)
      } finally {
        close(resource)
      }
    }
  }

  private[reactivemongo] class DefaultScopeFactory[Resource, Result]
      extends ScopeFactory[Resource, Result] {

    type OuterResult = Future[Result]

    def apply(
      open: () ⇒ Future[Resource],
      close: Resource ⇒ Unit
    ): Scope[Resource, Result, Future[Result]] =
      new DefaultScope[Resource, Result](open, close)
  }

  implicit def defaultScopeFactory[Resource, Result](implicit applied: Applied.Aux[Result, Future[Result]]): ScopeFactory.Aux[Resource, Result, applied.Outer] = new DefaultScopeFactory[Resource, Result]
}
