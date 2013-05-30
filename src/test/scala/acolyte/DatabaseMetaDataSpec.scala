package acolyte

import java.sql.ResultSet

import org.specs2.mutable.Specification

object DatabaseMetaDataSpec extends Specification with MetaDataFixtures {
  "Meta-data specification" title

  "Constructor" should {
    "refuse null owner" in {
      metadata(null) aka "constructor" must throwA[IllegalArgumentException]
    }
  }

  "Meta-data" should {
    "support transaction" in {
      metadata().supportsTransactions aka "flag" must beTrue
    }

    "support transaction isolation level" in {
      metadata().supportsTransactionIsolationLevel(-1) aka "flag" must beTrue
    }

    "not support savepoint" in {
      metadata().supportsSavepoints aka "flag" must beFalse
    }

    "support named parameters" in {
      metadata().supportsNamedParameters aka "flag" must beTrue
    }

    "not support savepoint" in {
      metadata().supportsSavepoints aka "flag" must beFalse
    }

    "not support multiple open results" in {
      metadata().supportsMultipleOpenResults aka "flag" must beFalse
    }

    "support getting generated keys" in {
      metadata().supportsGetGeneratedKeys aka "flag" must beTrue
    }

    "not support holding cursor over commit" in {
      metadata().
        supportsResultSetHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT).
        aka("holdability") must beFalse

    }

    "support closing cursor at commit" in {
      metadata().
        supportsResultSetHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT).
        aka("holdability") must beTrue

    }

    "using X/Open SQL states" in {
      metadata().getSQLStateType.
        aka("SQL state type") mustEqual java.sql.DatabaseMetaData.sqlStateXOpen

    }

    "not copy locators" in {
      metadata().locatorsUpdateCopy aka "flag" must beFalse
    }

    "not supports statement pooling" in {
      metadata().supportsStatementPooling aka "flag" must beFalse
    }

    "not supports row ID" in {
      metadata().getRowIdLifetime aka "row ID lifetime" mustEqual {
        java.sql.RowIdLifetime.ROWID_UNSUPPORTED
      }
    }

    "supports stored functions using call syntax" in {
      metadata().supportsStoredFunctionsUsingCallSyntax aka "flag" must beTrue
    }

    "not close all resultsets on failure" in {
      metadata().autoCommitFailureClosesAllResultSets aka "flag" must beFalse
    }

    "not always return generated keys" in {
      metadata().generatedKeyAlwaysReturned aka "flag" must beFalse
    }

    "not support CONVERT statement" >> {
      "without from/to types" in {
        metadata().supportsConvert aka "flag" must beFalse
      }

      "with from/to types" in {
        metadata().supportsConvert(1, 2) aka "flag" must beFalse
      }
    }
  }

  "Version" should {
    "be 4.0 for JDBC" in {
      lazy val m = metadata()

      (m.getJDBCMajorVersion aka "major version" mustEqual 4).
        and(m.getJDBCMinorVersion aka "minor version" mustEqual 0)

    }
  }
}

sealed trait MetaDataFixtures {
  import java.sql.Connection

  def metadata(c: Connection = new acolyte.Connection("jdbc:acolyte:test", null, "handler")) = new DatabaseMetaData(c)
}
