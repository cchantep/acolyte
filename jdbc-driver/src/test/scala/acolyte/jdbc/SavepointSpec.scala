package acolyte.jdbc

object SavepointSpec
    extends org.specs2.mutable.Specification with SavepointFixtures {

  "Savepoint specification".title

  "Un-named savepoint" should {
    "have null name" in {
      unnamed.getSavepointName aka "name" must beNull
    }
  }

  "Named savepoint" should {
    "have expected name" in {
      named("test").getSavepointName aka "name" must_=== "test"
    }
  }
}

sealed trait SavepointFixtures {
  def unnamed = new Savepoint()
  def named(n: String) = new Savepoint(n)
}
