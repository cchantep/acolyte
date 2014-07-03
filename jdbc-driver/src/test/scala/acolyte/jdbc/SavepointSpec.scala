package acolyte.jdbc

import org.specs2.mutable.Specification

object SavepointSpec extends Specification with SavepointFixtures {
  "Savepoint specification" title

  "Un-named savepoint" should {
    "have null name" in {
      Option(unnamed) aka "savepoint" must beSome.which { s ⇒
        s.getSavepointName aka "name" must beNull
      }
    }
  }

  "Named savepoint" should {
    "have expected name" in {
      Option(named("test")) aka "savepoint" must beSome.which { s ⇒
        s.getSavepointName aka "name" mustEqual "test"
      }
    }
  }
}

sealed trait SavepointFixtures {
  def unnamed = new Savepoint()
  def named(n: String) = new Savepoint(n)
}
