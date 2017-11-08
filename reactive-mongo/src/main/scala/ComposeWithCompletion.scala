package acolyte.reactivemongo

import scala.concurrent.{ ExecutionContext, Future }

sealed trait ComposeWithCompletion[Out] {
  type Outer <: Future[_]

  def apply[In](input: Future[In], f: In ⇒ Out)(onComplete: In ⇒ Unit)(implicit ec: ExecutionContext): Outer
}

object ComposeWithCompletion extends LowPriorityCompose {
  type Id[T] = ComposeWithCompletion[Future[T]] { type Outer = Future[T] }

  implicit def futureOut[T]: Id[T] = new ComposeWithCompletion[Future[T]] {
    type Outer = Future[T]

    def apply[In](input: Future[In], f: In ⇒ Future[T])(onComplete: In ⇒ Unit)(implicit ec: ExecutionContext): Future[T] = input.flatMap { in ⇒
      f(in).andThen {
        case _ ⇒ onComplete(in)
      }
    }

    override val toString = "futureOut"
  }
}

sealed trait LowPriorityCompose { _: ComposeWithCompletion.type ⇒
  type Map[T] = ComposeWithCompletion[T] { type Outer = Future[T] }

  implicit def pureOut[T]: Map[T] = new ComposeWithCompletion[T] {
    type Outer = Future[T]

    def apply[In](input: Future[In], f: In ⇒ T)(onComplete: In ⇒ Unit)(implicit ec: ExecutionContext): Future[T] = input.map { in ⇒
      try {
        f(in)
      } finally {
        onComplete(in)
      }
    }

    override val toString = "pureOut"
  }
}
