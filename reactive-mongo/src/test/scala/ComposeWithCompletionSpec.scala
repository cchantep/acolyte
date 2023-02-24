package acolyte.reactivemongo

import scala.concurrent.Future

import org.specs2.concurrent.ExecutionEnv

class ComposeWithCompletionSpec(
    implicit
    ee: ExecutionEnv)
    extends org.specs2.mutable.Specification {

  "Compose".title

  "From pure value" should {
    "work with a non-async function" in {
      def res: Future[Int] = pure("lorem")(_.size)

      res must beTypedEqualTo(5).await
    }

    "work with an async function" in {
      def res: Future[Int] = pure("bar")(str => Future.successful(str.size))

      res must beTypedEqualTo(3).await
    }
  }

  "From async value" should {
    "work with a non-async function" in {
      def res: Future[Int] = async(Future.successful("lorem"))(_.size)

      res must beTypedEqualTo(5).await
    }

    "work with an async function" in {
      def res: Future[Int] = async(Future.successful("bar")) { str =>
        Future.successful(str.size)
      }

      res must beTypedEqualTo(3).await
    }
  }

  // ---

  type Resource = String

  def pure[T](
      resource: Resource
    )(f: Resource => T
    )(implicit
      compose: ComposeWithCompletion[T]
    ): compose.Outer =
    compose(Future.successful(resource), f) { (_: Resource) => () }

  def async[T](
      resource: Future[Resource]
    )(f: Resource => T
    )(implicit
      compose: ComposeWithCompletion[T]
    ): compose.Outer = compose(resource, f) { (_: Resource) => () }
}
