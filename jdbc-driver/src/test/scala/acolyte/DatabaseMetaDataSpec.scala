package acolyte

import java.sql.ResultSet

import org.specs2.mutable.Specification

import acolyte.test.EmptyConnectionHandler

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

    "not support getting generated keys" in {
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

    "not support multiple result sets" in {
      metadata().supportsMultipleResultSets aka "flag" must beFalse
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
      lazy val c = new acolyte.Connection("jdbc:acolyte:meta", null, conHandler)
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

  "Procedures" should {
    lazy val procs = metadata().getProcedures("catalog", "schema", "proc")

    "have expected columns" in {
      lazy val meta = procs.getMetaData

      (meta.getColumnCount aka "count" mustEqual 8).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(7).
          aka("class #7") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(8).
          aka("class #8") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "PROCEDURE_CAT").
        and(meta.getColumnName(2) aka "name #2" mustEqual "PROCEDURE_SCHEM").
        and(meta.getColumnName(3) aka "name #3" mustEqual "PROCEDURE_NAME").
        and(meta.getColumnName(7) aka "name #7" mustEqual "REMARKS").
        and(meta.getColumnName(8) aka "name #8" mustEqual "PROCEDURE_TYPE")

    }

    "not be listed" in {
      (procs.getFetchSize aka "procedures" mustEqual 0).
        and(procs.next aka "next proc" must beFalse)

    }
  }

  "Procedure" should {
    lazy val proc = metadata().
      getProcedureColumns("catalog", "schema", "proc", "cols")

    "have expected columns" in {
      lazy val meta = proc.getMetaData

      (meta.getColumnCount aka "count" mustEqual 13).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(5).
          aka("class #5") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(6).
          aka("class #6") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(7).
          aka("class #7") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(8).
          aka("class #8") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(9).
          aka("class #9") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(10).
          aka("class #10") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(11).
          aka("class #11") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(12).
          aka("class #12") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(13).
          aka("class #13") mustEqual classOf[String].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "PROCEDURE_CAT").
        and(meta.getColumnName(2) aka "name #2" mustEqual "PROCEDURE_SCHEM").
        and(meta.getColumnName(3) aka "name #3" mustEqual "PROCEDURE_NAME").
        and(meta.getColumnName(4) aka "name #4" mustEqual "COLUMN_NAME").
        and(meta.getColumnName(5) aka "name #5" mustEqual "COLUMN_TYPE").
        and(meta.getColumnName(6) aka "name #6" mustEqual "DATA_TYPE").
        and(meta.getColumnName(7) aka "name #7" mustEqual "TYPE_NAME").
        and(meta.getColumnName(8) aka "name #8" mustEqual "PRECISION").
        and(meta.getColumnName(9) aka "name #9" mustEqual "LENGTH").
        and(meta.getColumnName(10) aka "name #10" mustEqual "SCALE").
        and(meta.getColumnName(11) aka "name #11" mustEqual "RADIX").
        and(meta.getColumnName(12) aka "name #12" mustEqual "NULLABLE").
        and(meta.getColumnName(13) aka "name #13" mustEqual "REMARKS")

    }

    "not be described" in {
      (proc.getFetchSize aka "procedure" mustEqual 0).
        and(proc.next aka "next col" must beFalse)

    }
  }

  "Tables" should {
    lazy val tables = metadata().
      getTables("catalog", "schema", "table", null)

    "have expected columns" in {
      lazy val meta = tables.getMetaData

      (meta.getColumnCount aka "count" mustEqual 10).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(5).
          aka("class #5") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(6).
          aka("class #6") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(7).
          aka("class #7") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(8).
          aka("class #8") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(9).
          aka("class #9") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(10).
          aka("class #10") mustEqual classOf[String].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "TABLE_CAT").
        and(meta.getColumnName(2) aka "name #2" mustEqual "TABLE_SCHEM").
        and(meta.getColumnName(3) aka "name #3" mustEqual "TABLE_NAME").
        and(meta.getColumnName(4) aka "name #4" mustEqual "TABLE_TYPE").
        and(meta.getColumnName(5) aka "name #5" mustEqual "REMARKS").
        and(meta.getColumnName(6) aka "name #6" mustEqual "TYPE_CAT").
        and(meta.getColumnName(7) aka "name #7" mustEqual "TYPE_SCHEM").
        and(meta.getColumnName(8) aka "name #8" mustEqual "TYPE_NAME").
        and(meta.getColumnName(9).
          aka("name #9") mustEqual "SELF_REFERENCING_COL_NAME").
        and(meta.getColumnName(10) aka "name #10" mustEqual "REF_GENERATION")

    }

    "not be listed" in {
      (tables.getFetchSize aka "tables" mustEqual 0).
        and(tables.next aka "next table" must beFalse)

    }
  }

  "Schemas" should {
    lazy val schemas = metadata().getSchemas

    "have expected columns" in {
      lazy val meta = schemas.getMetaData

      (meta.getColumnCount aka "count" mustEqual 2).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "TABLE_SCHEM").
        and(meta.getColumnName(2) aka "name #2" mustEqual "TABLE_CATALOG")

    }

    "not be listed" in {
      (schemas.getFetchSize aka "schemas" mustEqual 0).
        and(schemas.next aka "next schema" must beFalse)

    }
  }

  "Catalogs" should {
    lazy val catalogs = metadata().getCatalogs

    "have expected columns" in {
      lazy val meta = catalogs.getMetaData

      (meta.getColumnCount aka "count" mustEqual 1).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "TABLE_CAT")

    }

    "not be listed" in {
      (catalogs.getFetchSize aka "catalog" mustEqual 0).
        and(catalogs.next aka "next catalog" must beFalse)

    }
  }

  "Table types" should {
    lazy val types = metadata().getTableTypes

    "have expected columns" in {
      lazy val meta = types.getMetaData

      (meta.getColumnCount aka "count" mustEqual 1).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "TABLE_TYPE")

    }

    "not be listed" in {
      (types.getFetchSize aka "table types" mustEqual 0).
        and(types.next aka "next type" must beFalse)

    }
  }

  "Table" should {
    lazy val cols = metadata().
      getColumns("catalog", "schema", "table", "cols")

    lazy val pcols = metadata().
      getPseudoColumns("catalog", "schema", "table", "col")

    lazy val cprivs = metadata().
      getColumnPrivileges("catalog", "schema", "table", "cols")

    lazy val supr = metadata().getSuperTables("catalog", "schema", "table")

    lazy val tprivs = metadata().
      getTablePrivileges("catalog", "schema", "table")

    lazy val bestRowId = metadata().
      getBestRowIdentifier("catalog", "schema", "table", -1, false)

    lazy val verCols = metadata().
      getVersionColumns("catalog", "schema", "table")

    lazy val pkey = metadata().getPrimaryKeys("catalog", "schema", "table")
    lazy val ikeys = metadata().getImportedKeys("catalog", "schema", "table")
    lazy val ekeys = metadata().getExportedKeys("catalog", "schema", "table")
    lazy val index = metadata().
      getIndexInfo("catalog", "schema", "table", true, true)

    "have expected columns" in {
      lazy val meta = cols.getMetaData

      (meta.getColumnCount aka "count" mustEqual 21).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(5).
          aka("class #5") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(6).
          aka("class #6") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(7).
          aka("class #7") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(8).
          aka("class #8") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(9).
          aka("class #9") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(10).
          aka("class #10") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(11).
          aka("class #11") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(12).
          aka("class #12") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(13).
          aka("class #13") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(14).
          aka("class #14") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(15).
          aka("class #15") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(16).
          aka("class #16") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(17).
          aka("class #17") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(18).
          aka("class #18") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(19).
          aka("class #19") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(20).
          aka("class #20") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(21).
          aka("class #21") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "TABLE_CAT").
        and(meta.getColumnName(2) aka "name #2" mustEqual "TABLE_SCHEM").
        and(meta.getColumnName(3) aka "name #3" mustEqual "TABLE_NAME").
        and(meta.getColumnName(4) aka "name #4" mustEqual "COLUMN_NAME").
        and(meta.getColumnName(5) aka "name #5" mustEqual "DATA_TYPE").
        and(meta.getColumnName(6) aka "name #6" mustEqual "TYPE_NAME").
        and(meta.getColumnName(7) aka "name #7" mustEqual "BUFFER_LENGTH").
        and(meta.getColumnName(8) aka "name #8" mustEqual "DECIMAL_DIGITS").
        and(meta.getColumnName(9) aka "name #9" mustEqual "NUM_PREC_RADIX").
        and(meta.getColumnName(10) aka "name #10" mustEqual "NULLABLE").
        and(meta.getColumnName(11) aka "name #11" mustEqual "REMARKS").
        and(meta.getColumnName(12) aka "name #12" mustEqual "COLUMN_DEF").
        and(meta.getColumnName(13) aka "name #13" mustEqual "SQL_DATA_TYPE").
        and(meta.getColumnName(14) aka "name #14" mustEqual "SQL_DATETIME_SUB").
        and(meta.getColumnName(15).
          aka("name #15") mustEqual "CHAR_OCTET_LENGTH").
        and(meta.getColumnName(16) aka "name #16" mustEqual "ORDINAL_POSITION").
        and(meta.getColumnName(17) aka "name #17" mustEqual "IS_NULLABLE").
        and(meta.getColumnName(18) aka "name #18" mustEqual "SCOPE_CATLOG").
        and(meta.getColumnName(19) aka "name #19" mustEqual "SCOPE_SCHEMA").
        and(meta.getColumnName(20) aka "name #20" mustEqual "SCOPE_TABLE").
        and(meta.getColumnName(21) aka "name #21" mustEqual "SOURCE_DATA_TYPE")

    }

    "not have known columns" in {
      (cols.getFetchSize aka "table cols" mustEqual 0).
        and(cols.next aka "next col" must beFalse)

    }

    "not have pseudo columns" in {
      (pcols.getFetchSize aka "table cols" mustEqual 0).
        and(pcols.next aka "next col" must beFalse)

    }

    "have expected pseudo-columns" in {
      lazy val meta = pcols.getMetaData

      (meta.getColumnCount aka "count" mustEqual 8).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(5).
          aka("class #5") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(6).
          aka("class #6") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(7).
          aka("class #7") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(8).
          aka("class #8") mustEqual classOf[String].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "TABLE_CAT").
        and(meta.getColumnName(2) aka "name #2" mustEqual "TABLE_SCHEM").
        and(meta.getColumnName(3) aka "name #3" mustEqual "TABLE_NAME").
        and(meta.getColumnName(4) aka "name #4" mustEqual "COLUMN_NAME").
        and(meta.getColumnName(5) aka "name #5" mustEqual "GRANTOR").
        and(meta.getColumnName(6) aka "name #6" mustEqual "GRANTEE").
        and(meta.getColumnName(7) aka "name #7" mustEqual "PRIVILEGE").
        and(meta.getColumnName(8) aka "name #8" mustEqual "IS_GRANTABLE")

    }

    "not have column privileges" in {
      (cprivs.getFetchSize aka "col privileges" mustEqual 0).
        and(cprivs.next aka "next priv" must beFalse)

    }

    "have expected privileges columns" in {
      lazy val meta = cprivs.getMetaData

      (meta.getColumnCount aka "count" mustEqual 8).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(5).
          aka("class #5") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(6).
          aka("class #6") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(7).
          aka("class #7") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(8).
          aka("class #8") mustEqual classOf[String].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "TABLE_CAT").
        and(meta.getColumnName(2) aka "name #2" mustEqual "TABLE_SCHEM").
        and(meta.getColumnName(3) aka "name #3" mustEqual "TABLE_NAME").
        and(meta.getColumnName(4) aka "name #4" mustEqual "COLUMN_NAME").
        and(meta.getColumnName(5) aka "name #5" mustEqual "GRANTOR").
        and(meta.getColumnName(6) aka "name #6" mustEqual "GRANTEE").
        and(meta.getColumnName(7) aka "name #7" mustEqual "PRIVILEGE").
        and(meta.getColumnName(8) aka "name #8" mustEqual "IS_GRANTABLE")

    }

    "have expected super tables columns" in {
      lazy val meta = supr.getMetaData

      (meta.getColumnCount aka "count" mustEqual 4).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "TABLE_CAT").
        and(meta.getColumnName(2) aka "name #2" mustEqual "TABLE_SCHEM").
        and(meta.getColumnName(3) aka "name #3" mustEqual "TABLE_NAME").
        and(meta.getColumnName(4) aka "name #4" mustEqual "SUPERTABLE_NAME")

    }

    "not have super definitions" in {
      (supr.getFetchSize aka "definitions" mustEqual 0).
        and(supr.next aka "next table" must beFalse)

    }

    "have expected table privileges columns" in {
      lazy val meta = tprivs.getMetaData

      (meta.getColumnCount aka "count" mustEqual 7).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(5).
          aka("class #5") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(6).
          aka("class #6") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(7).
          aka("class #7") mustEqual classOf[String].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "TABLE_CAT").
        and(meta.getColumnName(2) aka "name #2" mustEqual "TABLE_SCHEM").
        and(meta.getColumnName(3) aka "name #3" mustEqual "TABLE_NAME").
        and(meta.getColumnName(4) aka "name #4" mustEqual "GRANTOR").
        and(meta.getColumnName(5) aka "name #5" mustEqual "GRANTEE").
        and(meta.getColumnName(6) aka "name #6" mustEqual "PRIVILEGE").
        and(meta.getColumnName(7) aka "name #7" mustEqual "IS_GRANTABLE")

    }

    "not have privileges" in {
      (tprivs.getFetchSize aka "col privileges" mustEqual 0).
        and(tprivs.next aka "next priv" must beFalse)

    }

    "have expected best row ID columns" in {
      lazy val meta = bestRowId.getMetaData

      (meta.getColumnCount aka "count" mustEqual 8).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(5).
          aka("class #5") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(6).
          aka("class #6") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(7).
          aka("class #7") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(8).
          aka("class #8") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "SCOPE").
        and(meta.getColumnName(2) aka "name #2" mustEqual "COLUMN_NAME").
        and(meta.getColumnName(3) aka "name #3" mustEqual "DATA_TYPE").
        and(meta.getColumnName(4) aka "name #4" mustEqual "TYPE_NAME").
        and(meta.getColumnName(5) aka "name #5" mustEqual "COLUMN_SIZE").
        and(meta.getColumnName(6) aka "name #6" mustEqual "BUFFER_LENGTH").
        and(meta.getColumnName(7) aka "name #7" mustEqual "DECIMAL_DIGITS").
        and(meta.getColumnName(8) aka "name #8" mustEqual "PSEUDO_COLUMN")

    }

    "not have best row identifier" in {
      (bestRowId.getFetchSize aka "best rowid" mustEqual 0).
        and(bestRowId.next aka "next rowid" must beFalse)

    }

    "have expected version columns" in {
      lazy val meta = verCols.getMetaData

      (meta.getColumnCount aka "count" mustEqual 8).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(5).
          aka("class #5") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(6).
          aka("class #6") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(7).
          aka("class #7") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(8).
          aka("class #8") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "SCOPE").
        and(meta.getColumnName(2) aka "name #2" mustEqual "COLUMN_NAME").
        and(meta.getColumnName(3) aka "name #3" mustEqual "DATA_TYPE").
        and(meta.getColumnName(4) aka "name #4" mustEqual "TYPE_NAME").
        and(meta.getColumnName(5) aka "name #5" mustEqual "COLUMN_SIZE").
        and(meta.getColumnName(6) aka "name #6" mustEqual "BUFFER_LENGTH").
        and(meta.getColumnName(7) aka "name #7" mustEqual "DECIMAL_DIGITS").
        and(meta.getColumnName(8) aka "name #8" mustEqual "PSEUDO_COLUMN")

    }

    "not have version columns" in {
      (verCols.getFetchSize aka "version columns" mustEqual 0).
        and(verCols.next aka "next column" must beFalse)

    }

    "have expected pkey columns" in {
      lazy val meta = pkey.getMetaData

      (meta.getColumnCount aka "count" mustEqual 6).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(5).
          aka("class #5") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(6).
          aka("class #6") mustEqual classOf[String].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "TABLE_CAT").
        and(meta.getColumnName(2) aka "name #2" mustEqual "TABLE_SCHEM").
        and(meta.getColumnName(3) aka "name #3" mustEqual "TABLE_NAME").
        and(meta.getColumnName(4) aka "name #4" mustEqual "COLUMN_NAME").
        and(meta.getColumnName(5) aka "name #5" mustEqual "KEY_SEQ").
        and(meta.getColumnName(6) aka "name #6" mustEqual "PK_NAME")

    }

    "not have primary key" in {
      (pkey.getFetchSize aka "primary keys" mustEqual 0).
        and(pkey.next aka "next key" must beFalse)

    }

    "have expected import key columns" in {
      lazy val meta = ikeys.getMetaData

      (meta.getColumnCount aka "count" mustEqual 14).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(5).
          aka("class #5") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(6).
          aka("class #6") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(7).
          aka("class #7") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(8).
          aka("class #8") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(9).
          aka("class #9") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(10).
          aka("class #10") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(11).
          aka("class #11") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(12).
          aka("class #12") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(13).
          aka("class #13") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(14).
          aka("class #14") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "PKTABLE_CAT").
        and(meta.getColumnName(2) aka "name #2" mustEqual "PKTABLE_SCHEM").
        and(meta.getColumnName(3) aka "name #3" mustEqual "PKTABLE_NAME").
        and(meta.getColumnName(4) aka "name #4" mustEqual "PKCOLUMN_NAME").
        and(meta.getColumnName(5) aka "name #5" mustEqual "FKTABLE_CAT").
        and(meta.getColumnName(6) aka "name #6" mustEqual "FKTABLE_SCHEM").
        and(meta.getColumnName(7) aka "name #7" mustEqual "FKTABLE_NAME").
        and(meta.getColumnName(8) aka "name #8" mustEqual "FKCOLUMN_NAME").
        and(meta.getColumnName(9) aka "name #9" mustEqual "KEY_SEQ").
        and(meta.getColumnName(10) aka "name #10" mustEqual "UPDATE_RULE").
        and(meta.getColumnName(11) aka "name #11" mustEqual "DELETE_RULE").
        and(meta.getColumnName(12) aka "name #12" mustEqual "FK_NAME").
        and(meta.getColumnName(13) aka "name #13" mustEqual "PK_NAME").
        and(meta.getColumnName(14) aka "name #14" mustEqual "DEFERRABILITY")

    }

    "not have imported key" in {
      (ikeys.getFetchSize aka "imported keys" mustEqual 0).
        and(ikeys.next aka "next key" must beFalse)

    }

    "have expected export key columns" in {
      lazy val meta = ekeys.getMetaData

      (meta.getColumnCount aka "count" mustEqual 14).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(5).
          aka("class #5") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(6).
          aka("class #6") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(7).
          aka("class #7") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(8).
          aka("class #8") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(9).
          aka("class #9") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(10).
          aka("class #10") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(11).
          aka("class #11") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(12).
          aka("class #12") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(13).
          aka("class #13") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(14).
          aka("class #14") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "PKTABLE_CAT").
        and(meta.getColumnName(2) aka "name #2" mustEqual "PKTABLE_SCHEM").
        and(meta.getColumnName(3) aka "name #3" mustEqual "PKTABLE_NAME").
        and(meta.getColumnName(4) aka "name #4" mustEqual "PKCOLUMN_NAME").
        and(meta.getColumnName(5) aka "name #5" mustEqual "FKTABLE_CAT").
        and(meta.getColumnName(6) aka "name #6" mustEqual "FKTABLE_SCHEM").
        and(meta.getColumnName(7) aka "name #7" mustEqual "FKTABLE_NAME").
        and(meta.getColumnName(8) aka "name #8" mustEqual "FKCOLUMN_NAME").
        and(meta.getColumnName(9) aka "name #9" mustEqual "KEY_SEQ").
        and(meta.getColumnName(10) aka "name #10" mustEqual "UPDATE_RULE").
        and(meta.getColumnName(11) aka "name #11" mustEqual "DELETE_RULE").
        and(meta.getColumnName(12) aka "name #12" mustEqual "FK_NAME").
        and(meta.getColumnName(13) aka "name #13" mustEqual "PK_NAME").
        and(meta.getColumnName(14) aka "name #14" mustEqual "DEFERRABILITY")

    }

    "not have exported key" in {
      (ekeys.getFetchSize aka "exported keys" mustEqual 0).
        and(ekeys.next aka "next key" must beFalse)

    }

    "have expected index columns" in {
      lazy val meta = index.getMetaData

      (meta.getColumnCount aka "count" mustEqual 13).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[java.lang.Boolean].getName).
        and(meta.getColumnClassName(5).
          aka("class #5") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(6).
          aka("class #6") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(7).
          aka("class #7") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(8).
          aka("class #8") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(9).
          aka("class #9") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(10).
          aka("class #10") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(11).
          aka("class #11") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(12).
          aka("class #12") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(13).
          aka("class #13") mustEqual classOf[String].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "TABLE_CAT").
        and(meta.getColumnName(2) aka "name #2" mustEqual "TABLE_SCHEM").
        and(meta.getColumnName(3) aka "name #3" mustEqual "TABLE_NAME").
        and(meta.getColumnName(4) aka "name #4" mustEqual "NON_UNIQUE").
        and(meta.getColumnName(5) aka "name #5" mustEqual "INDEX_QUALIFIER").
        and(meta.getColumnName(6) aka "name #6" mustEqual "INDEX_NAME").
        and(meta.getColumnName(7) aka "name #7" mustEqual "TYPE").
        and(meta.getColumnName(8) aka "name #8" mustEqual "ORDINAL_POSITION").
        and(meta.getColumnName(9) aka "name #9" mustEqual "COLUMN_NAME").
        and(meta.getColumnName(10) aka "name #10" mustEqual "ASC_OR_DESC").
        and(meta.getColumnName(11) aka "name #11" mustEqual "CARDINALITY").
        and(meta.getColumnName(12) aka "name #12" mustEqual "PAGES").
        and(meta.getColumnName(13) aka "name #13" mustEqual "FILTER_CONDITION")

    }

    "not have index info" in {
      (index.getFetchSize aka "index info" mustEqual 0).
        and(index.next aka "next info" must beFalse)

    }
  }

  "Cross reference" should {
    lazy val xref = metadata().getCrossReference(
      "pcat", "pschem", "ptable",
      "fcat", "fschem", "ftable")

    "have expected export key columns" in {
      lazy val meta = xref.getMetaData

      (meta.getColumnCount aka "count" mustEqual 14).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(5).
          aka("class #5") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(6).
          aka("class #6") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(7).
          aka("class #7") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(8).
          aka("class #8") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(9).
          aka("class #9") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(10).
          aka("class #10") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(11).
          aka("class #11") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(12).
          aka("class #12") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(13).
          aka("class #13") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(14).
          aka("class #14") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "PKTABLE_CAT").
        and(meta.getColumnName(2) aka "name #2" mustEqual "PKTABLE_SCHEM").
        and(meta.getColumnName(3) aka "name #3" mustEqual "PKTABLE_NAME").
        and(meta.getColumnName(4) aka "name #4" mustEqual "PKCOLUMN_NAME").
        and(meta.getColumnName(5) aka "name #5" mustEqual "FKTABLE_CAT").
        and(meta.getColumnName(6) aka "name #6" mustEqual "FKTABLE_SCHEM").
        and(meta.getColumnName(7) aka "name #7" mustEqual "FKTABLE_NAME").
        and(meta.getColumnName(8) aka "name #8" mustEqual "FKCOLUMN_NAME").
        and(meta.getColumnName(9) aka "name #9" mustEqual "KEY_SEQ").
        and(meta.getColumnName(10) aka "name #10" mustEqual "UPDATE_RULE").
        and(meta.getColumnName(11) aka "name #11" mustEqual "DELETE_RULE").
        and(meta.getColumnName(12) aka "name #12" mustEqual "FK_NAME").
        and(meta.getColumnName(13) aka "name #13" mustEqual "PK_NAME").
        and(meta.getColumnName(14) aka "name #14" mustEqual "DEFERRABILITY")

    }

    "not be known" in {
      (xref.getFetchSize aka "cross reference" mustEqual 0).
        and(xref.next aka "next ref" must beFalse)

    }
  }

  "Type information" should {
    lazy val info = metadata().getTypeInfo

    "have expected columns" in {
      lazy val meta = info.getMetaData

      (meta.getColumnCount aka "count" mustEqual 18).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(5).
          aka("class #5") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(6).
          aka("class #6") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(7).
          aka("class #7") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(8).
          aka("class #8") mustEqual classOf[java.lang.Boolean].getName).
        and(meta.getColumnClassName(9).
          aka("class #9") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(10).
          aka("class #10") mustEqual classOf[java.lang.Boolean].getName).
        and(meta.getColumnClassName(11).
          aka("class #11") mustEqual classOf[java.lang.Boolean].getName).
        and(meta.getColumnClassName(12).
          aka("class #12") mustEqual classOf[java.lang.Boolean].getName).
        and(meta.getColumnClassName(13).
          aka("class #13") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(14).
          aka("class #14") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(15).
          aka("class #15") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(16).
          aka("class #16") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(17).
          aka("class #17") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(18).
          aka("class #18") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "TYPE_NAME").
        and(meta.getColumnName(2) aka "name #2" mustEqual "DATA_TYPE").
        and(meta.getColumnName(3) aka "name #3" mustEqual "PRECISION").
        and(meta.getColumnName(4) aka "name #4" mustEqual "LITERAL_PREFIX").
        and(meta.getColumnName(5) aka "name #5" mustEqual "LITERAL_SUFFIX").
        and(meta.getColumnName(6) aka "name #6" mustEqual "CREATE_PARAMS").
        and(meta.getColumnName(7) aka "name #7" mustEqual "NULLABLE").
        and(meta.getColumnName(8) aka "name #8" mustEqual "CASE_SENSITIVE").
        and(meta.getColumnName(9) aka "name #9" mustEqual "SEARCHABLE").
        and(meta.getColumnName(10).
          aka("name #10") mustEqual "UNSIGNED_ATTRIBUTE").
        and(meta.getColumnName(11) aka "name #11" mustEqual "FIXED_PREC_SCALE").
        and(meta.getColumnName(12) aka "name #12" mustEqual "AUTO_INCREMENT").
        and(meta.getColumnName(13) aka "name #13" mustEqual "LOCAL_TYPE_NAME").
        and(meta.getColumnName(14) aka "name #14" mustEqual "MINIMUM_SCALE").
        and(meta.getColumnName(15) aka "name #15" mustEqual "MAXIMUM_SCALE").
        and(meta.getColumnName(16).
          aka("name #15") mustEqual "SQL_DATA_TYPE").
        and(meta.getColumnName(17) aka "name #16" mustEqual "SQL_DATETIME_SUB").
        and(meta.getColumnName(18) aka "name #18" mustEqual "NUM_PREC_RADIX")

    }

    "not be known" in {
      (info.getFetchSize aka "type info" mustEqual 0).
        and(info.next aka "next info" must beFalse)

    }
  }

  "Result sets" should {
    "support only for FORWARD type" in {
      lazy val meta = metadata()

      (meta.supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY).
        aka("forward") must beTrue).
        and(meta.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE).
          aka("insensitive scroll") must beFalse).
        and(meta.supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE).
          aka("sensitive scroll") must beFalse)

    }

    "support CONCUR_READ_ONLY" in {
      metadata().supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY,
        ResultSet.CONCUR_READ_ONLY) aka "concurrency" must beTrue

    }

    "have expected visibility of its own changes" in {
      lazy val meta = metadata()
      val t = ResultSet.TYPE_FORWARD_ONLY

      (meta.ownInsertsAreVisible(t) aka "insert" must beTrue).
        and(meta.ownUpdatesAreVisible(t) aka "update" must beTrue).
        and(meta.ownDeletesAreVisible(t) aka "delete" must beTrue)

    }

    "have expected visibility of its others changes" in {
      lazy val meta = metadata()
      val t = ResultSet.TYPE_FORWARD_ONLY

      (meta.othersInsertsAreVisible(t) aka "insert" must beFalse).
        and(meta.othersUpdatesAreVisible(t) aka "update" must beFalse).
        and(meta.othersDeletesAreVisible(t) aka "delete" must beFalse)

    }

    "be able to detect row change" in {
      lazy val meta = metadata()
      val t = ResultSet.TYPE_FORWARD_ONLY

      (meta.insertsAreDetected(t) aka "insert" must beTrue).
        and(meta.updatesAreDetected(t) aka "update" must beTrue).
        and(meta.deletesAreDetected(t) aka "delete" must beTrue)

    }
  }

  "Batch update" should {
    "be supported" in {
      metadata().supportsBatchUpdates aka "batch update" must beTrue
    }
  }

  "Types" should {
    lazy val udts = metadata().getUDTs("catalog", "schema", "type", null)
    lazy val supr = metadata().getSuperTypes("catalog", "schema", "type")

    "have expected UDTs columns" in {
      lazy val meta = udts.getMetaData

      (meta.getColumnCount aka "count" mustEqual 7).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(5).
          aka("class #5") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(6).
          aka("class #6") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(7).
          aka("class #7") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "TYPE_CAT").
        and(meta.getColumnName(2) aka "name #2" mustEqual "TYPE_SCHEM").
        and(meta.getColumnName(3) aka "name #3" mustEqual "TYPE_NAME").
        and(meta.getColumnName(4) aka "name #4" mustEqual "CLASS_NAME").
        and(meta.getColumnName(5) aka "name #5" mustEqual "DATA_TYPE").
        and(meta.getColumnName(6) aka "name #6" mustEqual "REMARKS").
        and(meta.getColumnName(7) aka "name #7" mustEqual "BASE_TYPE")

    }

    "known from user definitions" in {
      (udts.getFetchSize aka "UDTs" mustEqual 0).
        and(udts.next aka "next type" must beFalse)

    }

    "have expected pkey columns" in {
      lazy val meta = supr.getMetaData

      (meta.getColumnCount aka "count" mustEqual 6).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(5).
          aka("class #5") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(6).
          aka("class #6") mustEqual classOf[String].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "TYPE_CAT").
        and(meta.getColumnName(2) aka "name #2" mustEqual "TYPE_SCHEM").
        and(meta.getColumnName(3) aka "name #3" mustEqual "TYPE_NAME").
        and(meta.getColumnName(4) aka "name #4" mustEqual "SUPERTYPE_CAT").
        and(meta.getColumnName(5) aka "name #5" mustEqual "SUPERTYPE_SCHEM").
        and(meta.getColumnName(6) aka "name #6" mustEqual "SUPERTYPE_NAME")

    }

    "known from super definitions" in {
      (supr.getFetchSize aka "types" mustEqual 0).
        and(supr.next aka "next type" must beFalse)

    }
  }

  "Supported client info properties" should {
    lazy val clientInfo = metadata().getClientInfoProperties

    "have expected super tables columns" in {
      lazy val meta = clientInfo.getMetaData

      (meta.getColumnCount aka "count" mustEqual 4).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "NAME").
        and(meta.getColumnName(2) aka "name #2" mustEqual "MAX_LEN").
        and(meta.getColumnName(3) aka "name #3" mustEqual "DEFAULT_VALUE").
        and(meta.getColumnName(4) aka "name #4" mustEqual "DESCRIPTION")

    }

    "be expected one" in {
      (clientInfo.getFetchSize aka "client info" mustEqual 0).
        and(clientInfo.next aka "next property" must beFalse)

    }
  }

  "Attributes" should {
    lazy val attrs = metadata().
      getAttributes("catalog", "schema", "type", "attr")

    "have expected attributes columns" in {
      lazy val meta = attrs.getMetaData

      (meta.getColumnCount aka "count" mustEqual 21).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(5).
          aka("class #5") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(6).
          aka("class #6") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(7).
          aka("class #7") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(8).
          aka("class #8") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(9).
          aka("class #9") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(10).
          aka("class #10") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(11).
          aka("class #11") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(12).
          aka("class #12") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(13).
          aka("class #13") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(14).
          aka("class #14") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(15).
          aka("class #15") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(16).
          aka("class #16") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(17).
          aka("class #17") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(18).
          aka("class #18") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(19).
          aka("class #19") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(20).
          aka("class #20") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(21).
          aka("class #21") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "TYPE_CAT").
        and(meta.getColumnName(2) aka "name #2" mustEqual "TYPE_SCHEM").
        and(meta.getColumnName(3) aka "name #3" mustEqual "TYPE_NAME").
        and(meta.getColumnName(4) aka "name #4" mustEqual "ATTR_NAME").
        and(meta.getColumnName(5) aka "name #5" mustEqual "DATA_TYPE").
        and(meta.getColumnName(6) aka "name #6" mustEqual "ATTR_TYPE_NAME").
        and(meta.getColumnName(7) aka "name #7" mustEqual "ATTR_SIZE").
        and(meta.getColumnName(8) aka "name #8" mustEqual "DECIMAL_DIGITS").
        and(meta.getColumnName(9) aka "name #9" mustEqual "NUM_PREC_RADIX").
        and(meta.getColumnName(10) aka "name #10" mustEqual "NULLABLE").
        and(meta.getColumnName(11) aka "name #11" mustEqual "REMARKS").
        and(meta.getColumnName(12) aka "name #12" mustEqual "ATTR_DEF").
        and(meta.getColumnName(13) aka "name #13" mustEqual "SQL_DATA_TYPE").
        and(meta.getColumnName(14) aka "name #14" mustEqual "SQL_DATETIME_SUB").
        and(meta.getColumnName(15).
          aka("name #15") mustEqual "CHAR_OCTET_LENGTH").
        and(meta.getColumnName(16) aka "name #16" mustEqual "ORDINAL_POSITION").
        and(meta.getColumnName(17) aka "name #17" mustEqual "IS_NULLABLE").
        and(meta.getColumnName(18) aka "name #18" mustEqual "SCOPE_CATLOG").
        and(meta.getColumnName(19) aka "name #19" mustEqual "SCOPE_SCHEMA").
        and(meta.getColumnName(20) aka "name #20" mustEqual "SCOPE_TABLE").
        and(meta.getColumnName(21) aka "name #21" mustEqual "SOURCE_DATA_TYPE")

    }

    "be expected one" in {
      (attrs.getFetchSize aka "attributes" mustEqual 0).
        and(attrs.next aka "next attr" must beFalse)

    }
  }

  "Schemas" should {
    lazy val schemas = metadata().getSchemas("catalog", "schema")

    "have expected columns" in {
      lazy val meta = schemas.getMetaData

      (meta.getColumnCount aka "count" mustEqual 2).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "TABLE_SCHEM").
        and(meta.getColumnName(2) aka "name #2" mustEqual "TABLE_CATALOG")

    }

    "not be known" in {
      (schemas.getFetchSize aka "schemas" mustEqual 0).
        and(schemas.next aka "next schema" must beFalse)

    }
  }

  "Functions" should {
    lazy val funcs = metadata().getFunctions("catalog", "schema", "func")
    lazy val cols = metadata().
      getFunctionColumns("catalog", "schema", "func", "col")

    "have expected function columns" in {
      lazy val meta = funcs.getMetaData

      (meta.getColumnCount aka "count" mustEqual 6).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(5).
          aka("class #5") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(6).
          aka("class #6") mustEqual classOf[String].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "FUNCTION_CAT").
        and(meta.getColumnName(2) aka "name #2" mustEqual "FUNCTION_SCHEM").
        and(meta.getColumnName(3) aka "name #3" mustEqual "FUNCTION_NAME").
        and(meta.getColumnName(4) aka "name #4" mustEqual "REMARKS").
        and(meta.getColumnName(5) aka "name #5" mustEqual "FUNCTION_TYPE").
        and(meta.getColumnName(6) aka "name #6" mustEqual "SPECIFIC_NAME")

    }

    "not be listed" in {
      (funcs.getFetchSize aka "functions" mustEqual 0).
        and(funcs.next aka "next function" must beFalse)

    }

    "have expected columns" in {
      lazy val meta = cols.getMetaData

      (meta.getColumnCount aka "count" mustEqual 17).
        and(meta.getColumnClassName(1).
          aka("class #1") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(2).
          aka("class #2") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(3).
          aka("class #3") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(4).
          aka("class #4") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(5).
          aka("class #5") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(6).
          aka("class #6") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(7).
          aka("class #7") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(8).
          aka("class #8") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(9).
          aka("class #9") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(10).
          aka("class #10") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(11).
          aka("class #11") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(12).
          aka("class #12") mustEqual classOf[java.lang.Short].getName).
        and(meta.getColumnClassName(13).
          aka("class #13") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(14).
          aka("class #14") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(15).
          aka("class #15") mustEqual classOf[java.lang.Integer].getName).
        and(meta.getColumnClassName(16).
          aka("class #16") mustEqual classOf[String].getName).
        and(meta.getColumnClassName(17).
          aka("class #17") mustEqual classOf[String].getName).
        and(meta.getColumnName(1) aka "name #1" mustEqual "FUNCTION_CAT").
        and(meta.getColumnName(2) aka "name #2" mustEqual "FUNCTION_SCHEM").
        and(meta.getColumnName(3) aka "name #3" mustEqual "FUNCTION_NAME").
        and(meta.getColumnName(4) aka "name #4" mustEqual "COLUMN_NAME").
        and(meta.getColumnName(5) aka "name #5" mustEqual "COLUMN_TYPE").
        and(meta.getColumnName(6) aka "name #6" mustEqual "DATA_TYPE").
        and(meta.getColumnName(7) aka "name #7" mustEqual "TYPE_NAME").
        and(meta.getColumnName(8) aka "name #8" mustEqual "PRECISION").
        and(meta.getColumnName(9) aka "name #9" mustEqual "LENGTH").
        and(meta.getColumnName(10) aka "name #10" mustEqual "SCALE").
        and(meta.getColumnName(11) aka "name #11" mustEqual "RADIX").
        and(meta.getColumnName(12) aka "name #12" mustEqual "NULLABLE").
        and(meta.getColumnName(13) aka "name #13" mustEqual "REMARKS").
        and(meta.getColumnName(14).
          aka("name #14") mustEqual "CHAR_OCTET_LENGTH").
        and(meta.getColumnName(15) aka "name #15" mustEqual "ORDINAL_POSITION").
        and(meta.getColumnName(16).
          aka("name #16") mustEqual "IS_NULLABLE").
        and(meta.getColumnName(17) aka "name #17" mustEqual "SPECIFIC_NAME")

    }

    "not be described" in {
      (cols.getFetchSize aka "function cols" mustEqual 0).
        and(cols.next aka "next col" must beFalse)

    }
  }
}

sealed trait MetaDataFixtures {
  val conHandler = EmptyConnectionHandler

  def metadata(c: Connection = new acolyte.Connection("jdbc:acolyte:test", null, conHandler)) = new DatabaseMetaData(c)
}
