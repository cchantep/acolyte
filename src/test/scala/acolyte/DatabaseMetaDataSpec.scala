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

    "have all procedures callable" in {
      metadata().allProceduresAreCallable aka "flag" must beTrue
    }

    "have all tables selectable" in {
      metadata().allTablesAreSelectable aka "flag" must beTrue
    }

    "have expected username" in {
      metadata().getUserName aka "username" mustEqual "acolyte"
    }

    "not be read-only" in {
      metadata().isReadOnly aka "read-only" must beFalse
    }

    "not sort NULLs as high" in {
      lazy val m = metadata()

      (m.nullsAreSortedHigh aka "high sort" must beFalse).
        and(m.nullsAreSortedLow aka "low sort" must beTrue).
        and(m.nullsAreSortedAtStart aka "at start" must beFalse).
        and(m.nullsAreSortedAtEnd aka "at end" must beTrue)
    }

    "have expected product info" in {
      lazy val m = metadata()

      (m.getDatabaseProductName aka "name" mustEqual "Acolyte").
        and(m.getDatabaseProductVersion aka "version" mustEqual "0.1-beta")
    }

    "have expected driver info" in {
      lazy val m = metadata()
      import Driver.{ MAJOR_VERSION, MINOR_VERSION }

      (m.getDriverName aka "name" mustEqual "acolyte").
        and(m.getDriverMajorVersion.
          aka("major version") mustEqual MAJOR_VERSION).
        and(m.getDriverMinorVersion.
          aka("minor version") mustEqual MINOR_VERSION).
        and(m.getDriverVersion.
          aka("version") mustEqual "%s.%s".format(MAJOR_VERSION, MINOR_VERSION))

    }

    "not use local files" in {
      (metadata().usesLocalFiles aka "global flag" must beFalse).
        and(metadata().usesLocalFilePerTable aka "table flag" must beFalse)
    }

    "have expected identifier case support" in {
      lazy val m = metadata()

      (m.supportsMixedCaseIdentifiers aka "mixed support" must beTrue).
        and(m.storesUpperCaseIdentifiers aka "store upper" must beFalse).
        and(m.storesLowerCaseIdentifiers aka "store lower" must beFalse).
        and(m.storesMixedCaseIdentifiers aka "store mixed" must beTrue).
        and(m.supportsMixedCaseQuotedIdentifiers.
          aka("quoted mixed support") must beTrue).
        and(m.storesUpperCaseQuotedIdentifiers.
          aka("quoted store upper") must beFalse).
        and(m.storesLowerCaseQuotedIdentifiers.
          aka("quoted store lower") must beFalse).
        and(m.storesMixedCaseQuotedIdentifiers.
          aka("quoted store mixed") must beTrue)

    }

    "have expected identifier quote" in {
      metadata().getIdentifierQuoteString aka "quote" mustEqual "`"
    }

    "have expected SQL keywords" in {
      metadata().getSQLKeywords aka "keywords" mustEqual ""
    }

    "have expected functions" in {
      lazy val m = metadata()

      (m.getNumericFunctions aka "list" mustEqual "").
        and(m.getStringFunctions aka "list" mustEqual "").
        and(m.getSystemFunctions aka "list" mustEqual "").
        and(m.getTimeDateFunctions aka "list" mustEqual "")
    }

    "have expected search escape" in {
      metadata().getSearchStringEscape aka "string" mustEqual "\\"
    }

    "have no extra name character" in {
      metadata().getExtraNameCharacters aka "chars" mustEqual ""
    }

    "support ALTER TABLE with ADD COLUMN" in {
      metadata().supportsAlterTableWithAddColumn aka "flag" must beTrue
    }

    "support ALTER TABLE with DROP COLUMN" in {
      metadata().supportsAlterTableWithDropColumn aka "flag" must beTrue
    }

    "support column aliasing" in {
      metadata().supportsColumnAliasing aka "flag" must beTrue
    }

    "consider NULL + NULL = NULL" in {
      metadata().nullPlusNonNullIsNull aka "flag" must beTrue
    }

    "support table correlation names" in {
      metadata().supportsTableCorrelationNames aka "flag" must beTrue
    }

    "support different table correlation names" in {
      metadata().supportsDifferentTableCorrelationNames aka "flag" must beTrue
    }

    "support expressions in ORDER BY" in {
      metadata().supportsExpressionsInOrderBy aka "flag" must beTrue
    }

    "support ORDER BY unrelated" in {
      metadata().supportsOrderByUnrelated aka "flag" must beTrue
    }

    "support GROUP BY" in {
      metadata().supportsGroupBy aka "flag" must beTrue
    }

    "support GROUP BY unrelated" in {
      metadata().supportsGroupByUnrelated aka "flag" must beTrue
    }

    "support GROUP BY beyond SELECT" in {
      metadata().supportsGroupByBeyondSelect aka "flag" must beTrue
    }

    "support LIKE espace clause" in {
      metadata().supportsLikeEscapeClause aka "flag" must beTrue
    }

    "support multiple result sets" in {
      metadata().supportsMultipleResultSets aka "flag" must beTrue
    }

    "support multiple transactions" in {
      metadata().supportsMultipleTransactions aka "flag" must beTrue
    }

    "support non-nullable columns" in {
      metadata().supportsNonNullableColumns aka "flag" must beTrue
    }

    "support min SQL grammar" in {
      metadata().supportsMinimumSQLGrammar aka "flag" must beTrue
    }

    "support core SQL grammar" in {
      metadata().supportsCoreSQLGrammar aka "flag" must beTrue
    }

    "support extended SQL grammar" in {
      metadata().supportsExtendedSQLGrammar aka "flag" must beTrue
    }

    "support ANSI92 entry level SQL" in {
      metadata().supportsANSI92EntryLevelSQL aka "flag" must beTrue
    }

    "support ANSI92 intermediate SQL" in {
      metadata().supportsANSI92IntermediateSQL aka "flag" must beTrue
    }

    "support ANSI92 full SQL" in {
      metadata().supportsANSI92FullSQL aka "flag" must beTrue
    }

    "support integrity enhancement facility" in {
      metadata().supportsIntegrityEnhancementFacility aka "flag" must beTrue
    }

    "support OUTER JOIN" in {
      metadata().supportsOuterJoins aka "flag" must beTrue
    }

    "support full OUTER JOIN" in {
      metadata().supportsFullOuterJoins aka "flag" must beTrue
    }

    "support limited OUTER JOIN" in {
      metadata().supportsLimitedOuterJoins aka "flag" must beTrue
    }

    "use expected terms" in {
      lazy val m = metadata()

      (m.getSchemaTerm aka "schema term" mustEqual "schema").
        and(m.getProcedureTerm aka "procedure term" mustEqual "procedure").
        and(m.getCatalogTerm aka "catalog term" mustEqual "catalog")

    }

    "have expected catalog separator" in {
      metadata().getCatalogSeparator aka "separator" mustEqual "."
    }

    "supports SchemasInDataManipulation" in {
      metadata().supportsSchemasInDataManipulation aka "flag" must beTrue
    }

    "supports SchemasInProcedureCalls" in {
      metadata().supportsSchemasInProcedureCalls aka "flag" must beTrue
    }

    "supports SchemasInTableDefinitions" in {
      metadata().supportsSchemasInTableDefinitions aka "flag" must beTrue
    }

    "supports SchemasInIndexDefinitions" in {
      metadata().supportsSchemasInIndexDefinitions aka "flag" must beTrue
    }

    "supports SchemasInPrivilegeDefinitions" in {
      metadata().supportsSchemasInPrivilegeDefinitions aka "flag" must beTrue
    }

    "supports CatalogsInDataManipulation" in {
      metadata().supportsCatalogsInDataManipulation aka "flag" must beTrue
    }

    "supports CatalogsInProcedureCalls" in {
      metadata().supportsCatalogsInProcedureCalls aka "flag" must beTrue
    }

    "supports CatalogsInTableDefinitions" in {
      metadata().supportsCatalogsInTableDefinitions aka "flag" must beTrue
    }

    "supports CatalogsInIndexDefinitions" in {
      metadata().supportsCatalogsInIndexDefinitions aka "flag" must beTrue
    }

    "supports CatalogsInPrivilegeDefinitions" in {
      metadata().supportsCatalogsInPrivilegeDefinitions aka "flag" must beTrue
    }

    "supports PositionedDelete" in {
      metadata().supportsPositionedDelete aka "flag" must beTrue
    }

    "supports PositionedUpdate" in {
      metadata().supportsPositionedUpdate aka "flag" must beTrue
    }

    "supports SelectForUpdate" in {
      metadata().supportsSelectForUpdate aka "flag" must beTrue
    }

    "supports StoredProcedures" in {
      metadata().supportsStoredProcedures aka "flag" must beTrue
    }

    "supports SubqueriesInComparisons" in {
      metadata().supportsSubqueriesInComparisons aka "flag" must beTrue
    }

    "supports SubqueriesInExists" in {
      metadata().supportsSubqueriesInExists aka "flag" must beTrue
    }

    "supports SubqueriesInIns" in {
      metadata().supportsSubqueriesInIns aka "flag" must beTrue
    }

    "supports SubqueriesInQuantifieds" in {
      metadata().supportsSubqueriesInQuantifieds aka "flag" must beTrue
    }

    "supports CorrelatedSubqueries" in {
      metadata().supportsCorrelatedSubqueries aka "flag" must beTrue
    }

    "supports Union" in {
      metadata().supportsUnion aka "flag" must beTrue
    }

    "supports UnionAll" in {
      metadata().supportsUnionAll aka "flag" must beTrue
    }

    "supports OpenCursorsAcrossCommit" in {
      metadata().supportsOpenCursorsAcrossCommit aka "flag" must beTrue
    }

    "supports OpenCursorsAcrossRollback" in {
      metadata().supportsOpenCursorsAcrossRollback aka "flag" must beTrue
    }

    "supports OpenStatementsAcrossCommit" in {
      metadata().supportsOpenStatementsAcrossCommit aka "flag" must beTrue
    }

    "supports OpenStatementsAcrossRollback" in {
      metadata().supportsOpenStatementsAcrossRollback aka "flag" must beTrue
    }

    "have no max limit" in {
      lazy val m = metadata()

      (m.getMaxBinaryLiteralLength aka "bin length" mustEqual 0).
        and(m.getMaxCharLiteralLength aka "char length" mustEqual 0).
        and(m.getMaxColumnNameLength aka "colname length" mustEqual 0).
        and(m.getMaxColumnsInGroupBy aka "grouped by cols" mustEqual 0).
        and(m.getMaxColumnsInIndex aka "indexed cols" mustEqual 0).
        and(m.getMaxColumnsInOrderBy aka "ordered cols" mustEqual 0).
        and(m.getMaxColumnsInSelect aka "selected cols" mustEqual 0).
        and(m.getMaxColumnsInTable aka "table cols" mustEqual 0).
        and(m.getMaxConnections aka "connections" mustEqual 0).
        and(m.getMaxCursorNameLength aka "cursor name" mustEqual 0).
        and(m.getMaxIndexLength aka "index" mustEqual 0).
        and(m.getMaxSchemaNameLength aka "schema name" mustEqual 0).
        and(m.getMaxProcedureNameLength aka "procedure name" mustEqual 0).
        and(m.getMaxCatalogNameLength aka "catalog name" mustEqual 0).
        and(m.getMaxRowSize aka "row size" mustEqual 0).
        and(m.getMaxStatementLength aka "statement length" mustEqual 0).
        and(m.getMaxStatements aka "statements" mustEqual 0).
        and(m.getMaxTableNameLength aka "table name" mustEqual 0).
        and(m.getMaxTablesInSelect aka "selected tables" mustEqual 0).
        and(m.getMaxUserNameLength aka "username" mustEqual 0)
    }

    "have NONE as default transaction isolation" in {
      metadata().getDefaultTransactionIsolation.
        aka("isolation") mustEqual java.sql.Connection.TRANSACTION_NONE

    }

    "supports DataDefinitionAndDataManipulationTransactions" in {
      metadata().supportsDataDefinitionAndDataManipulationTransactions.
        aka("flag") must beTrue

    }

    "not supports data manipulation only in transaction" in {
      metadata().supportsDataManipulationTransactionsOnly.
        aka("flag") must beFalse

    }
  }

  "Data definition" should {
    "not cause commit" in {
      metadata().dataDefinitionCausesTransactionCommit aka "flag" must beFalse
    }

    "not be ignored in transaction" in {
      metadata().dataDefinitionIgnoredInTransactions aka "flag" must beFalse
    }
  }

  "Max row size" should {
    "include blobs" in {
      metadata().doesMaxRowSizeIncludeBlobs aka "flag" must beTrue
    }
  }

  "Catalog" should {
    "be at start of fully qualified table name" in {
      metadata().isCatalogAtStart aka "flag" must beTrue
    }
  }

  "Owner connection" should {
    "be attached to related meta-data" in {
      lazy val c = new acolyte.Connection("jdbc:acolyte:meta", null, "handler")
      lazy val m = metadata(c)

      c.setReadOnly(true)

      (m.getConnection aka "meta-data owner" mustEqual c).
        and(m.getURL aka "RDBMS URL" mustEqual "jdbc:acolyte:meta").
        and(m.isReadOnly aka "read-only mode" must beTrue)

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
  def metadata(c: Connection = new acolyte.Connection("jdbc:acolyte:test", null, "handler")) = new DatabaseMetaData(c)
}
