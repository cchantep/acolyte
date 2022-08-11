package acolyte.jdbc

import java.sql.ResultSet

import org.specs2.mutable.Specification

import acolyte.jdbc.test.EmptyConnectionHandler

object DatabaseMetaDataSpec extends Specification with MetaDataFixtures {
  "Meta-data specification".title

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
      metadata()
        .supportsResultSetHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT)
        .aka("holdability") must beFalse

    }

    "support closing cursor at commit" in {
      metadata()
        .supportsResultSetHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT)
        .aka("holdability") must beTrue

    }

    "using X/Open SQL states" in {
      metadata().getSQLStateType
        .aka("SQL state type") must_=== java.sql.DatabaseMetaData.sqlStateXOpen

    }

    "not copy locators" in {
      metadata().locatorsUpdateCopy aka "flag" must beFalse
    }

    "not supports statement pooling" in {
      metadata().supportsStatementPooling aka "flag" must beFalse
    }

    "not supports row ID" in {
      metadata().getRowIdLifetime aka "row ID lifetime" must_=== {
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
      metadata().getUserName aka "username" must_=== "acolyte"
    }

    "not be read-only" in {
      metadata().isReadOnly aka "read-only" must beFalse
    }

    "not sort NULLs as high" in {
      lazy val m = metadata()

      (m.nullsAreSortedHigh aka "high sort" must beFalse)
        .and(m.nullsAreSortedLow aka "low sort" must beTrue)
        .and(m.nullsAreSortedAtStart aka "at start" must beFalse)
        .and(m.nullsAreSortedAtEnd aka "at end" must beTrue)
    }

    "have expected product info" in {
      lazy val m = metadata()

      (m.getDatabaseProductName aka "name" must_=== "Acolyte")
        .and(m.getDatabaseProductVersion aka "version" must_=== "0.1-beta")
    }

    "have expected driver info" in {
      lazy val m = metadata()
      import Driver.{ MAJOR_VERSION, MINOR_VERSION }

      (m.getDriverName aka "name" must_=== "acolyte")
        .and(
          m.getDriverMajorVersion.aka("major version") must_=== MAJOR_VERSION
        )
        .and(
          m.getDriverMinorVersion.aka("minor version") must_=== MINOR_VERSION
        )
        .and(
          m.getDriverVersion.aka("version") must_=== "%s.%s"
            .format(MAJOR_VERSION, MINOR_VERSION)
        )

    }

    "not use local files" in {
      (metadata().usesLocalFiles aka "global flag" must beFalse)
        .and(metadata().usesLocalFilePerTable aka "table flag" must beFalse)
    }

    "have expected identifier case support" in {
      lazy val m = metadata()

      (m.supportsMixedCaseIdentifiers aka "mixed support" must beTrue)
        .and(m.storesUpperCaseIdentifiers aka "store upper" must beFalse)
        .and(m.storesLowerCaseIdentifiers aka "store lower" must beFalse)
        .and(m.storesMixedCaseIdentifiers aka "store mixed" must beTrue)
        .and(
          m.supportsMixedCaseQuotedIdentifiers.aka(
            "quoted mixed support"
          ) must beTrue
        )
        .and(
          m.storesUpperCaseQuotedIdentifiers
            .aka("quoted store upper") must beFalse
        )
        .and(
          m.storesLowerCaseQuotedIdentifiers
            .aka("quoted store lower") must beFalse
        )
        .and(
          m.storesMixedCaseQuotedIdentifiers
            .aka("quoted store mixed") must beTrue
        )

    }

    "have expected identifier quote" in {
      metadata().getIdentifierQuoteString aka "quote" must_=== "`"
    }

    "have expected SQL keywords" in {
      metadata().getSQLKeywords aka "keywords" must_=== ""
    }

    "have expected functions" in {
      lazy val m = metadata()

      (m.getNumericFunctions aka "list" must_=== "")
        .and(m.getStringFunctions aka "list" must_=== "")
        .and(m.getSystemFunctions aka "list" must_=== "")
        .and(m.getTimeDateFunctions aka "list" must_=== "")
    }

    "have expected search escape" in {
      metadata().getSearchStringEscape aka "string" must_=== "\\"
    }

    "have no extra name character" in {
      metadata().getExtraNameCharacters aka "chars" must_=== ""
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

      (m.getSchemaTerm aka "schema term" must_=== "schema")
        .and(m.getProcedureTerm aka "procedure term" must_=== "procedure")
        .and(m.getCatalogTerm aka "catalog term" must_=== "catalog")

    }

    "have expected catalog separator" in {
      metadata().getCatalogSeparator aka "separator" must_=== "."
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

      (m.getMaxBinaryLiteralLength aka "bin length" must_=== 0)
        .and(m.getMaxCharLiteralLength aka "char length" must_=== 0)
        .and(m.getMaxColumnNameLength aka "colname length" must_=== 0)
        .and(m.getMaxColumnsInGroupBy aka "grouped by cols" must_=== 0)
        .and(m.getMaxColumnsInIndex aka "indexed cols" must_=== 0)
        .and(m.getMaxColumnsInOrderBy aka "ordered cols" must_=== 0)
        .and(m.getMaxColumnsInSelect aka "selected cols" must_=== 0)
        .and(m.getMaxColumnsInTable aka "table cols" must_=== 0)
        .and(m.getMaxConnections aka "connections" must_=== 0)
        .and(m.getMaxCursorNameLength aka "cursor name" must_=== 0)
        .and(m.getMaxIndexLength aka "index" must_=== 0)
        .and(m.getMaxSchemaNameLength aka "schema name" must_=== 0)
        .and(m.getMaxProcedureNameLength aka "procedure name" must_=== 0)
        .and(m.getMaxCatalogNameLength aka "catalog name" must_=== 0)
        .and(m.getMaxRowSize aka "row size" must_=== 0)
        .and(m.getMaxStatementLength aka "statement length" must_=== 0)
        .and(m.getMaxStatements aka "statements" must_=== 0)
        .and(m.getMaxTableNameLength aka "table name" must_=== 0)
        .and(m.getMaxTablesInSelect aka "selected tables" must_=== 0)
        .and(m.getMaxUserNameLength aka "username" must_=== 0)
    }

    "have NONE as default transaction isolation" in {
      metadata().getDefaultTransactionIsolation
        .aka("isolation") must_=== java.sql.Connection.TRANSACTION_NONE

    }

    "supports DataDefinitionAndDataManipulationTransactions" in {
      metadata().supportsDataDefinitionAndDataManipulationTransactions
        .aka("flag") must beTrue

    }

    "not supports data manipulation only in transaction" in {
      metadata().supportsDataManipulationTransactionsOnly
        .aka("flag") must beFalse

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
      lazy val c =
        new acolyte.jdbc.Connection("jdbc:acolyte:meta", null, conHandler)
      lazy val m = metadata(c)

      c.setReadOnly(true)

      (m.getConnection aka "meta-data owner" must_=== c)
        .and(m.getURL aka "RDBMS URL" must_=== "jdbc:acolyte:meta")
        .and(m.isReadOnly aka "read-only mode" must beTrue)

    }
  }

  "Version" should {
    "be 4.0 for JDBC" in {
      lazy val m = metadata()

      (m.getJDBCMajorVersion aka "major version" must_=== 4)
        .and(m.getJDBCMinorVersion aka "minor version" must_=== 0)

    }
  }

  "Procedures" should {
    lazy val procs = metadata().getProcedures("catalog", "schema", "proc")

    "have expected columns" in {
      lazy val meta = procs.getMetaData

      (meta.getColumnCount aka "count" must_=== 8)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(7)
            .aka("class #7") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(8)
            .aka("class #8") must_=== classOf[java.lang.Short].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "PROCEDURE_CAT")
        .and(meta.getColumnName(2) aka "name #2" must_=== "PROCEDURE_SCHEM")
        .and(meta.getColumnName(3) aka "name #3" must_=== "PROCEDURE_NAME")
        .and(meta.getColumnName(7) aka "name #7" must_=== "REMARKS")
        .and(meta.getColumnName(8) aka "name #8" must_=== "PROCEDURE_TYPE")

    }

    "not be listed" in {
      (procs.getFetchSize aka "procedures" must_=== 0)
        .and(procs.next aka "next proc" must beFalse)

    }
  }

  "Procedure" should {
    lazy val proc =
      metadata().getProcedureColumns("catalog", "schema", "proc", "cols")

    "have expected columns" in {
      lazy val meta = proc.getMetaData

      (meta.getColumnCount aka "count" must_=== 13)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(5)
            .aka("class #5") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(6)
            .aka("class #6") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(7)
            .aka("class #7") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(8)
            .aka("class #8") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(9)
            .aka("class #9") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(10)
            .aka("class #10") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(11)
            .aka("class #11") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(12)
            .aka("class #12") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(13)
            .aka("class #13") must_=== classOf[String].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "PROCEDURE_CAT")
        .and(meta.getColumnName(2) aka "name #2" must_=== "PROCEDURE_SCHEM")
        .and(meta.getColumnName(3) aka "name #3" must_=== "PROCEDURE_NAME")
        .and(meta.getColumnName(4) aka "name #4" must_=== "COLUMN_NAME")
        .and(meta.getColumnName(5) aka "name #5" must_=== "COLUMN_TYPE")
        .and(meta.getColumnName(6) aka "name #6" must_=== "DATA_TYPE")
        .and(meta.getColumnName(7) aka "name #7" must_=== "TYPE_NAME")
        .and(meta.getColumnName(8) aka "name #8" must_=== "PRECISION")
        .and(meta.getColumnName(9) aka "name #9" must_=== "LENGTH")
        .and(meta.getColumnName(10) aka "name #10" must_=== "SCALE")
        .and(meta.getColumnName(11) aka "name #11" must_=== "RADIX")
        .and(meta.getColumnName(12) aka "name #12" must_=== "NULLABLE")
        .and(meta.getColumnName(13) aka "name #13" must_=== "REMARKS")

    }

    "not be described" in {
      (proc.getFetchSize aka "procedure" must_=== 0)
        .and(proc.next aka "next col" must beFalse)

    }
  }

  "Tables" should {
    lazy val tables = metadata().getTables("catalog", "schema", "table", null)

    "have expected columns" in {
      lazy val meta = tables.getMetaData

      (meta.getColumnCount aka "count" must_=== 10)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(5)
            .aka("class #5") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(6)
            .aka("class #6") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(7)
            .aka("class #7") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(8)
            .aka("class #8") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(9)
            .aka("class #9") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(10)
            .aka("class #10") must_=== classOf[String].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "TABLE_CAT")
        .and(meta.getColumnName(2) aka "name #2" must_=== "TABLE_SCHEM")
        .and(meta.getColumnName(3) aka "name #3" must_=== "TABLE_NAME")
        .and(meta.getColumnName(4) aka "name #4" must_=== "TABLE_TYPE")
        .and(meta.getColumnName(5) aka "name #5" must_=== "REMARKS")
        .and(meta.getColumnName(6) aka "name #6" must_=== "TYPE_CAT")
        .and(meta.getColumnName(7) aka "name #7" must_=== "TYPE_SCHEM")
        .and(meta.getColumnName(8) aka "name #8" must_=== "TYPE_NAME")
        .and(
          meta
            .getColumnName(9)
            .aka("name #9") must_=== "SELF_REFERENCING_COL_NAME"
        )
        .and(meta.getColumnName(10) aka "name #10" must_=== "REF_GENERATION")

    }

    "not be listed" in {
      (tables.getFetchSize aka "tables" must_=== 0)
        .and(tables.next aka "next table" must beFalse)

    }
  }

  "Schemas" should {
    lazy val schemas = metadata().getSchemas

    "have expected columns" in {
      lazy val meta = schemas.getMetaData

      (meta.getColumnCount aka "count" must_=== 2)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "TABLE_SCHEM")
        .and(meta.getColumnName(2) aka "name #2" must_=== "TABLE_CATALOG")

    }

    "not be listed" in {
      (schemas.getFetchSize aka "schemas" must_=== 0)
        .and(schemas.next aka "next schema" must beFalse)

    }
  }

  "Catalogs" should {
    lazy val catalogs = metadata().getCatalogs

    "have expected columns" in {
      lazy val meta = catalogs.getMetaData

      (meta.getColumnCount aka "count" must_=== 1)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "TABLE_CAT")

    }

    "not be listed" in {
      (catalogs.getFetchSize aka "catalog" must_=== 0)
        .and(catalogs.next aka "next catalog" must beFalse)

    }
  }

  "Table types" should {
    lazy val types = metadata().getTableTypes

    "have expected columns" in {
      lazy val meta = types.getMetaData

      (meta.getColumnCount aka "count" must_=== 1)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "TABLE_TYPE")

    }

    "not be listed" in {
      (types.getFetchSize aka "table types" must_=== 0)
        .and(types.next aka "next type" must beFalse)

    }
  }

  "Table" should {
    lazy val cols = metadata().getColumns("catalog", "schema", "table", "cols")

    lazy val pcols =
      metadata().getPseudoColumns("catalog", "schema", "table", "col")

    lazy val cprivs =
      metadata().getColumnPrivileges("catalog", "schema", "table", "cols")

    lazy val supr = metadata().getSuperTables("catalog", "schema", "table")

    lazy val tprivs =
      metadata().getTablePrivileges("catalog", "schema", "table")

    lazy val bestRowId =
      metadata().getBestRowIdentifier("catalog", "schema", "table", -1, false)

    lazy val verCols =
      metadata().getVersionColumns("catalog", "schema", "table")

    lazy val pkey = metadata().getPrimaryKeys("catalog", "schema", "table")
    lazy val ikeys = metadata().getImportedKeys("catalog", "schema", "table")
    lazy val ekeys = metadata().getExportedKeys("catalog", "schema", "table")
    lazy val index =
      metadata().getIndexInfo("catalog", "schema", "table", true, true)

    "have expected columns" in {
      lazy val meta = cols.getMetaData

      (meta.getColumnCount aka "count" must_=== 21)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(5)
            .aka("class #5") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(6)
            .aka("class #6") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(7)
            .aka("class #7") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(8)
            .aka("class #8") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(9)
            .aka("class #9") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(10)
            .aka("class #10") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(11)
            .aka("class #11") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(12)
            .aka("class #12") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(13)
            .aka("class #13") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(14)
            .aka("class #14") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(15)
            .aka("class #15") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(16)
            .aka("class #16") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(17)
            .aka("class #17") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(18)
            .aka("class #18") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(19)
            .aka("class #19") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(20)
            .aka("class #20") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(21)
            .aka("class #21") must_=== classOf[java.lang.Short].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "TABLE_CAT")
        .and(meta.getColumnName(2) aka "name #2" must_=== "TABLE_SCHEM")
        .and(meta.getColumnName(3) aka "name #3" must_=== "TABLE_NAME")
        .and(meta.getColumnName(4) aka "name #4" must_=== "COLUMN_NAME")
        .and(meta.getColumnName(5) aka "name #5" must_=== "DATA_TYPE")
        .and(meta.getColumnName(6) aka "name #6" must_=== "TYPE_NAME")
        .and(meta.getColumnName(7) aka "name #7" must_=== "BUFFER_LENGTH")
        .and(meta.getColumnName(8) aka "name #8" must_=== "DECIMAL_DIGITS")
        .and(meta.getColumnName(9) aka "name #9" must_=== "NUM_PREC_RADIX")
        .and(meta.getColumnName(10) aka "name #10" must_=== "NULLABLE")
        .and(meta.getColumnName(11) aka "name #11" must_=== "REMARKS")
        .and(meta.getColumnName(12) aka "name #12" must_=== "COLUMN_DEF")
        .and(meta.getColumnName(13) aka "name #13" must_=== "SQL_DATA_TYPE")
        .and(meta.getColumnName(14) aka "name #14" must_=== "SQL_DATETIME_SUB")
        .and(
          meta.getColumnName(15).aka("name #15") must_=== "CHAR_OCTET_LENGTH"
        )
        .and(meta.getColumnName(16) aka "name #16" must_=== "ORDINAL_POSITION")
        .and(meta.getColumnName(17) aka "name #17" must_=== "IS_NULLABLE")
        .and(meta.getColumnName(18) aka "name #18" must_=== "SCOPE_CATLOG")
        .and(meta.getColumnName(19) aka "name #19" must_=== "SCOPE_SCHEMA")
        .and(meta.getColumnName(20) aka "name #20" must_=== "SCOPE_TABLE")
        .and(meta.getColumnName(21) aka "name #21" must_=== "SOURCE_DATA_TYPE")

    }

    "not have known columns" in {
      (cols.getFetchSize aka "table cols" must_=== 0)
        .and(cols.next aka "next col" must beFalse)

    }

    "not have pseudo columns" in {
      (pcols.getFetchSize aka "table cols" must_=== 0)
        .and(pcols.next aka "next col" must beFalse)

    }

    "have expected pseudo-columns" in {
      lazy val meta = pcols.getMetaData

      (meta.getColumnCount aka "count" must_=== 8)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(5)
            .aka("class #5") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(6)
            .aka("class #6") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(7)
            .aka("class #7") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(8)
            .aka("class #8") must_=== classOf[String].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "TABLE_CAT")
        .and(meta.getColumnName(2) aka "name #2" must_=== "TABLE_SCHEM")
        .and(meta.getColumnName(3) aka "name #3" must_=== "TABLE_NAME")
        .and(meta.getColumnName(4) aka "name #4" must_=== "COLUMN_NAME")
        .and(meta.getColumnName(5) aka "name #5" must_=== "GRANTOR")
        .and(meta.getColumnName(6) aka "name #6" must_=== "GRANTEE")
        .and(meta.getColumnName(7) aka "name #7" must_=== "PRIVILEGE")
        .and(meta.getColumnName(8) aka "name #8" must_=== "IS_GRANTABLE")

    }

    "not have column privileges" in {
      (cprivs.getFetchSize aka "col privileges" must_=== 0)
        .and(cprivs.next aka "next priv" must beFalse)

    }

    "have expected privileges columns" in {
      lazy val meta = cprivs.getMetaData

      (meta.getColumnCount aka "count" must_=== 8)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(5)
            .aka("class #5") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(6)
            .aka("class #6") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(7)
            .aka("class #7") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(8)
            .aka("class #8") must_=== classOf[String].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "TABLE_CAT")
        .and(meta.getColumnName(2) aka "name #2" must_=== "TABLE_SCHEM")
        .and(meta.getColumnName(3) aka "name #3" must_=== "TABLE_NAME")
        .and(meta.getColumnName(4) aka "name #4" must_=== "COLUMN_NAME")
        .and(meta.getColumnName(5) aka "name #5" must_=== "GRANTOR")
        .and(meta.getColumnName(6) aka "name #6" must_=== "GRANTEE")
        .and(meta.getColumnName(7) aka "name #7" must_=== "PRIVILEGE")
        .and(meta.getColumnName(8) aka "name #8" must_=== "IS_GRANTABLE")

    }

    "have expected super tables columns" in {
      lazy val meta = supr.getMetaData

      (meta.getColumnCount aka "count" must_=== 4)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "TABLE_CAT")
        .and(meta.getColumnName(2) aka "name #2" must_=== "TABLE_SCHEM")
        .and(meta.getColumnName(3) aka "name #3" must_=== "TABLE_NAME")
        .and(meta.getColumnName(4) aka "name #4" must_=== "SUPERTABLE_NAME")

    }

    "not have super definitions" in {
      (supr.getFetchSize aka "definitions" must_=== 0)
        .and(supr.next aka "next table" must beFalse)

    }

    "have expected table privileges columns" in {
      lazy val meta = tprivs.getMetaData

      (meta.getColumnCount aka "count" must_=== 7)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(5)
            .aka("class #5") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(6)
            .aka("class #6") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(7)
            .aka("class #7") must_=== classOf[String].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "TABLE_CAT")
        .and(meta.getColumnName(2) aka "name #2" must_=== "TABLE_SCHEM")
        .and(meta.getColumnName(3) aka "name #3" must_=== "TABLE_NAME")
        .and(meta.getColumnName(4) aka "name #4" must_=== "GRANTOR")
        .and(meta.getColumnName(5) aka "name #5" must_=== "GRANTEE")
        .and(meta.getColumnName(6) aka "name #6" must_=== "PRIVILEGE")
        .and(meta.getColumnName(7) aka "name #7" must_=== "IS_GRANTABLE")

    }

    "not have privileges" in {
      (tprivs.getFetchSize aka "col privileges" must_=== 0)
        .and(tprivs.next aka "next priv" must beFalse)

    }

    "have expected best row ID columns" in {
      lazy val meta = bestRowId.getMetaData

      (meta.getColumnCount aka "count" must_=== 8)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(5)
            .aka("class #5") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(6)
            .aka("class #6") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(7)
            .aka("class #7") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(8)
            .aka("class #8") must_=== classOf[java.lang.Short].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "SCOPE")
        .and(meta.getColumnName(2) aka "name #2" must_=== "COLUMN_NAME")
        .and(meta.getColumnName(3) aka "name #3" must_=== "DATA_TYPE")
        .and(meta.getColumnName(4) aka "name #4" must_=== "TYPE_NAME")
        .and(meta.getColumnName(5) aka "name #5" must_=== "COLUMN_SIZE")
        .and(meta.getColumnName(6) aka "name #6" must_=== "BUFFER_LENGTH")
        .and(meta.getColumnName(7) aka "name #7" must_=== "DECIMAL_DIGITS")
        .and(meta.getColumnName(8) aka "name #8" must_=== "PSEUDO_COLUMN")

    }

    "not have best row identifier" in {
      (bestRowId.getFetchSize aka "best rowid" must_=== 0)
        .and(bestRowId.next aka "next rowid" must beFalse)

    }

    "have expected version columns" in {
      lazy val meta = verCols.getMetaData

      (meta.getColumnCount aka "count" must_=== 8)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(5)
            .aka("class #5") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(6)
            .aka("class #6") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(7)
            .aka("class #7") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(8)
            .aka("class #8") must_=== classOf[java.lang.Short].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "SCOPE")
        .and(meta.getColumnName(2) aka "name #2" must_=== "COLUMN_NAME")
        .and(meta.getColumnName(3) aka "name #3" must_=== "DATA_TYPE")
        .and(meta.getColumnName(4) aka "name #4" must_=== "TYPE_NAME")
        .and(meta.getColumnName(5) aka "name #5" must_=== "COLUMN_SIZE")
        .and(meta.getColumnName(6) aka "name #6" must_=== "BUFFER_LENGTH")
        .and(meta.getColumnName(7) aka "name #7" must_=== "DECIMAL_DIGITS")
        .and(meta.getColumnName(8) aka "name #8" must_=== "PSEUDO_COLUMN")

    }

    "not have version columns" in {
      (verCols.getFetchSize aka "version columns" must_=== 0)
        .and(verCols.next aka "next column" must beFalse)

    }

    "have expected pkey columns" in {
      lazy val meta = pkey.getMetaData

      (meta.getColumnCount aka "count" must_=== 6)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(5)
            .aka("class #5") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(6)
            .aka("class #6") must_=== classOf[String].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "TABLE_CAT")
        .and(meta.getColumnName(2) aka "name #2" must_=== "TABLE_SCHEM")
        .and(meta.getColumnName(3) aka "name #3" must_=== "TABLE_NAME")
        .and(meta.getColumnName(4) aka "name #4" must_=== "COLUMN_NAME")
        .and(meta.getColumnName(5) aka "name #5" must_=== "KEY_SEQ")
        .and(meta.getColumnName(6) aka "name #6" must_=== "PK_NAME")

    }

    "not have primary key" in {
      (pkey.getFetchSize aka "primary keys" must_=== 0)
        .and(pkey.next aka "next key" must beFalse)

    }

    "have expected import key columns" in {
      lazy val meta = ikeys.getMetaData

      (meta.getColumnCount aka "count" must_=== 14)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(5)
            .aka("class #5") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(6)
            .aka("class #6") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(7)
            .aka("class #7") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(8)
            .aka("class #8") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(9)
            .aka("class #9") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(10)
            .aka("class #10") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(11)
            .aka("class #11") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(12)
            .aka("class #12") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(13)
            .aka("class #13") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(14)
            .aka("class #14") must_=== classOf[java.lang.Short].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "PKTABLE_CAT")
        .and(meta.getColumnName(2) aka "name #2" must_=== "PKTABLE_SCHEM")
        .and(meta.getColumnName(3) aka "name #3" must_=== "PKTABLE_NAME")
        .and(meta.getColumnName(4) aka "name #4" must_=== "PKCOLUMN_NAME")
        .and(meta.getColumnName(5) aka "name #5" must_=== "FKTABLE_CAT")
        .and(meta.getColumnName(6) aka "name #6" must_=== "FKTABLE_SCHEM")
        .and(meta.getColumnName(7) aka "name #7" must_=== "FKTABLE_NAME")
        .and(meta.getColumnName(8) aka "name #8" must_=== "FKCOLUMN_NAME")
        .and(meta.getColumnName(9) aka "name #9" must_=== "KEY_SEQ")
        .and(meta.getColumnName(10) aka "name #10" must_=== "UPDATE_RULE")
        .and(meta.getColumnName(11) aka "name #11" must_=== "DELETE_RULE")
        .and(meta.getColumnName(12) aka "name #12" must_=== "FK_NAME")
        .and(meta.getColumnName(13) aka "name #13" must_=== "PK_NAME")
        .and(meta.getColumnName(14) aka "name #14" must_=== "DEFERRABILITY")

    }

    "not have imported key" in {
      (ikeys.getFetchSize aka "imported keys" must_=== 0)
        .and(ikeys.next aka "next key" must beFalse)

    }

    "have expected export key columns" in {
      lazy val meta = ekeys.getMetaData

      (meta.getColumnCount aka "count" must_=== 14)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(5)
            .aka("class #5") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(6)
            .aka("class #6") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(7)
            .aka("class #7") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(8)
            .aka("class #8") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(9)
            .aka("class #9") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(10)
            .aka("class #10") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(11)
            .aka("class #11") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(12)
            .aka("class #12") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(13)
            .aka("class #13") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(14)
            .aka("class #14") must_=== classOf[java.lang.Short].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "PKTABLE_CAT")
        .and(meta.getColumnName(2) aka "name #2" must_=== "PKTABLE_SCHEM")
        .and(meta.getColumnName(3) aka "name #3" must_=== "PKTABLE_NAME")
        .and(meta.getColumnName(4) aka "name #4" must_=== "PKCOLUMN_NAME")
        .and(meta.getColumnName(5) aka "name #5" must_=== "FKTABLE_CAT")
        .and(meta.getColumnName(6) aka "name #6" must_=== "FKTABLE_SCHEM")
        .and(meta.getColumnName(7) aka "name #7" must_=== "FKTABLE_NAME")
        .and(meta.getColumnName(8) aka "name #8" must_=== "FKCOLUMN_NAME")
        .and(meta.getColumnName(9) aka "name #9" must_=== "KEY_SEQ")
        .and(meta.getColumnName(10) aka "name #10" must_=== "UPDATE_RULE")
        .and(meta.getColumnName(11) aka "name #11" must_=== "DELETE_RULE")
        .and(meta.getColumnName(12) aka "name #12" must_=== "FK_NAME")
        .and(meta.getColumnName(13) aka "name #13" must_=== "PK_NAME")
        .and(meta.getColumnName(14) aka "name #14" must_=== "DEFERRABILITY")

    }

    "not have exported key" in {
      (ekeys.getFetchSize aka "exported keys" must_=== 0)
        .and(ekeys.next aka "next key" must beFalse)

    }

    "have expected index columns" in {
      lazy val meta = index.getMetaData

      (meta.getColumnCount aka "count" must_=== 13)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[java.lang.Boolean].getName
        )
        .and(
          meta
            .getColumnClassName(5)
            .aka("class #5") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(6)
            .aka("class #6") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(7)
            .aka("class #7") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(8)
            .aka("class #8") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(9)
            .aka("class #9") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(10)
            .aka("class #10") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(11)
            .aka("class #11") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(12)
            .aka("class #12") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(13)
            .aka("class #13") must_=== classOf[String].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "TABLE_CAT")
        .and(meta.getColumnName(2) aka "name #2" must_=== "TABLE_SCHEM")
        .and(meta.getColumnName(3) aka "name #3" must_=== "TABLE_NAME")
        .and(meta.getColumnName(4) aka "name #4" must_=== "NON_UNIQUE")
        .and(meta.getColumnName(5) aka "name #5" must_=== "INDEX_QUALIFIER")
        .and(meta.getColumnName(6) aka "name #6" must_=== "INDEX_NAME")
        .and(meta.getColumnName(7) aka "name #7" must_=== "TYPE")
        .and(meta.getColumnName(8) aka "name #8" must_=== "ORDINAL_POSITION")
        .and(meta.getColumnName(9) aka "name #9" must_=== "COLUMN_NAME")
        .and(meta.getColumnName(10) aka "name #10" must_=== "ASC_OR_DESC")
        .and(meta.getColumnName(11) aka "name #11" must_=== "CARDINALITY")
        .and(meta.getColumnName(12) aka "name #12" must_=== "PAGES")
        .and(meta.getColumnName(13) aka "name #13" must_=== "FILTER_CONDITION")

    }

    "not have index info" in {
      (index.getFetchSize aka "index info" must_=== 0)
        .and(index.next aka "next info" must beFalse)

    }
  }

  "Cross reference" should {
    lazy val xref = metadata().getCrossReference(
      "pcat",
      "pschem",
      "ptable",
      "fcat",
      "fschem",
      "ftable"
    )

    "have expected export key columns" in {
      lazy val meta = xref.getMetaData

      (meta.getColumnCount aka "count" must_=== 14)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(5)
            .aka("class #5") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(6)
            .aka("class #6") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(7)
            .aka("class #7") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(8)
            .aka("class #8") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(9)
            .aka("class #9") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(10)
            .aka("class #10") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(11)
            .aka("class #11") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(12)
            .aka("class #12") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(13)
            .aka("class #13") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(14)
            .aka("class #14") must_=== classOf[java.lang.Short].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "PKTABLE_CAT")
        .and(meta.getColumnName(2) aka "name #2" must_=== "PKTABLE_SCHEM")
        .and(meta.getColumnName(3) aka "name #3" must_=== "PKTABLE_NAME")
        .and(meta.getColumnName(4) aka "name #4" must_=== "PKCOLUMN_NAME")
        .and(meta.getColumnName(5) aka "name #5" must_=== "FKTABLE_CAT")
        .and(meta.getColumnName(6) aka "name #6" must_=== "FKTABLE_SCHEM")
        .and(meta.getColumnName(7) aka "name #7" must_=== "FKTABLE_NAME")
        .and(meta.getColumnName(8) aka "name #8" must_=== "FKCOLUMN_NAME")
        .and(meta.getColumnName(9) aka "name #9" must_=== "KEY_SEQ")
        .and(meta.getColumnName(10) aka "name #10" must_=== "UPDATE_RULE")
        .and(meta.getColumnName(11) aka "name #11" must_=== "DELETE_RULE")
        .and(meta.getColumnName(12) aka "name #12" must_=== "FK_NAME")
        .and(meta.getColumnName(13) aka "name #13" must_=== "PK_NAME")
        .and(meta.getColumnName(14) aka "name #14" must_=== "DEFERRABILITY")

    }

    "not be known" in {
      (xref.getFetchSize aka "cross reference" must_=== 0)
        .and(xref.next aka "next ref" must beFalse)

    }
  }

  "Type information" should {
    lazy val info = metadata().getTypeInfo

    "have expected columns" in {
      lazy val meta = info.getMetaData

      (meta.getColumnCount aka "count" must_=== 18)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(5)
            .aka("class #5") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(6)
            .aka("class #6") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(7)
            .aka("class #7") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(8)
            .aka("class #8") must_=== classOf[java.lang.Boolean].getName
        )
        .and(
          meta
            .getColumnClassName(9)
            .aka("class #9") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(10)
            .aka("class #10") must_=== classOf[java.lang.Boolean].getName
        )
        .and(
          meta
            .getColumnClassName(11)
            .aka("class #11") must_=== classOf[java.lang.Boolean].getName
        )
        .and(
          meta
            .getColumnClassName(12)
            .aka("class #12") must_=== classOf[java.lang.Boolean].getName
        )
        .and(
          meta
            .getColumnClassName(13)
            .aka("class #13") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(14)
            .aka("class #14") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(15)
            .aka("class #15") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(16)
            .aka("class #16") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(17)
            .aka("class #17") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(18)
            .aka("class #18") must_=== classOf[java.lang.Integer].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "TYPE_NAME")
        .and(meta.getColumnName(2) aka "name #2" must_=== "DATA_TYPE")
        .and(meta.getColumnName(3) aka "name #3" must_=== "PRECISION")
        .and(meta.getColumnName(4) aka "name #4" must_=== "LITERAL_PREFIX")
        .and(meta.getColumnName(5) aka "name #5" must_=== "LITERAL_SUFFIX")
        .and(meta.getColumnName(6) aka "name #6" must_=== "CREATE_PARAMS")
        .and(meta.getColumnName(7) aka "name #7" must_=== "NULLABLE")
        .and(meta.getColumnName(8) aka "name #8" must_=== "CASE_SENSITIVE")
        .and(meta.getColumnName(9) aka "name #9" must_=== "SEARCHABLE")
        .and(
          meta.getColumnName(10).aka("name #10") must_=== "UNSIGNED_ATTRIBUTE"
        )
        .and(meta.getColumnName(11) aka "name #11" must_=== "FIXED_PREC_SCALE")
        .and(meta.getColumnName(12) aka "name #12" must_=== "AUTO_INCREMENT")
        .and(meta.getColumnName(13) aka "name #13" must_=== "LOCAL_TYPE_NAME")
        .and(meta.getColumnName(14) aka "name #14" must_=== "MINIMUM_SCALE")
        .and(meta.getColumnName(15) aka "name #15" must_=== "MAXIMUM_SCALE")
        .and(meta.getColumnName(16).aka("name #15") must_=== "SQL_DATA_TYPE")
        .and(meta.getColumnName(17) aka "name #16" must_=== "SQL_DATETIME_SUB")
        .and(meta.getColumnName(18) aka "name #18" must_=== "NUM_PREC_RADIX")

    }

    "not be known" in {
      (info.getFetchSize aka "type info" must_=== 0)
        .and(info.next aka "next info" must beFalse)

    }
  }

  "Result sets" should {
    "support only for FORWARD type" in {
      lazy val meta = metadata()

      (meta
        .supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY)
        .aka("forward") must beTrue)
        .and(
          meta
            .supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE)
            .aka("insensitive scroll") must beFalse
        )
        .and(
          meta
            .supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE)
            .aka("sensitive scroll") must beFalse
        )

    }

    "support CONCUR_READ_ONLY" in {
      metadata().supportsResultSetConcurrency(
        ResultSet.TYPE_FORWARD_ONLY,
        ResultSet.CONCUR_READ_ONLY
      ) aka "concurrency" must beTrue

    }

    "have expected visibility of its own changes" in {
      lazy val meta = metadata()
      val t = ResultSet.TYPE_FORWARD_ONLY

      (meta.ownInsertsAreVisible(t) aka "insert" must beTrue)
        .and(meta.ownUpdatesAreVisible(t) aka "update" must beTrue)
        .and(meta.ownDeletesAreVisible(t) aka "delete" must beTrue)

    }

    "have expected visibility of its others changes" in {
      lazy val meta = metadata()
      val t = ResultSet.TYPE_FORWARD_ONLY

      (meta.othersInsertsAreVisible(t) aka "insert" must beFalse)
        .and(meta.othersUpdatesAreVisible(t) aka "update" must beFalse)
        .and(meta.othersDeletesAreVisible(t) aka "delete" must beFalse)

    }

    "be able to detect row change" in {
      lazy val meta = metadata()
      val t = ResultSet.TYPE_FORWARD_ONLY

      (meta.insertsAreDetected(t) aka "insert" must beTrue)
        .and(meta.updatesAreDetected(t) aka "update" must beTrue)
        .and(meta.deletesAreDetected(t) aka "delete" must beTrue)

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

      (meta.getColumnCount aka "count" must_=== 7)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(5)
            .aka("class #5") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(6)
            .aka("class #6") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(7)
            .aka("class #7") must_=== classOf[java.lang.Short].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "TYPE_CAT")
        .and(meta.getColumnName(2) aka "name #2" must_=== "TYPE_SCHEM")
        .and(meta.getColumnName(3) aka "name #3" must_=== "TYPE_NAME")
        .and(meta.getColumnName(4) aka "name #4" must_=== "CLASS_NAME")
        .and(meta.getColumnName(5) aka "name #5" must_=== "DATA_TYPE")
        .and(meta.getColumnName(6) aka "name #6" must_=== "REMARKS")
        .and(meta.getColumnName(7) aka "name #7" must_=== "BASE_TYPE")

    }

    "known from user definitions" in {
      (udts.getFetchSize aka "UDTs" must_=== 0)
        .and(udts.next aka "next type" must beFalse)

    }

    "have expected pkey columns" in {
      lazy val meta = supr.getMetaData

      (meta.getColumnCount aka "count" must_=== 6)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(5)
            .aka("class #5") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(6)
            .aka("class #6") must_=== classOf[String].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "TYPE_CAT")
        .and(meta.getColumnName(2) aka "name #2" must_=== "TYPE_SCHEM")
        .and(meta.getColumnName(3) aka "name #3" must_=== "TYPE_NAME")
        .and(meta.getColumnName(4) aka "name #4" must_=== "SUPERTYPE_CAT")
        .and(meta.getColumnName(5) aka "name #5" must_=== "SUPERTYPE_SCHEM")
        .and(meta.getColumnName(6) aka "name #6" must_=== "SUPERTYPE_NAME")

    }

    "known from super definitions" in {
      (supr.getFetchSize aka "types" must_=== 0)
        .and(supr.next aka "next type" must beFalse)

    }
  }

  "Supported client info properties" should {
    lazy val clientInfo = metadata().getClientInfoProperties

    "have expected super tables columns" in {
      lazy val meta = clientInfo.getMetaData

      (meta.getColumnCount aka "count" must_=== 4)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "NAME")
        .and(meta.getColumnName(2) aka "name #2" must_=== "MAX_LEN")
        .and(meta.getColumnName(3) aka "name #3" must_=== "DEFAULT_VALUE")
        .and(meta.getColumnName(4) aka "name #4" must_=== "DESCRIPTION")

    }

    "be expected one" in {
      (clientInfo.getFetchSize aka "client info" must_=== 0)
        .and(clientInfo.next aka "next property" must beFalse)

    }
  }

  "Attributes" should {
    lazy val attrs =
      metadata().getAttributes("catalog", "schema", "type", "attr")

    "have expected attributes columns" in {
      lazy val meta = attrs.getMetaData

      (meta.getColumnCount aka "count" must_=== 21)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(5)
            .aka("class #5") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(6)
            .aka("class #6") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(7)
            .aka("class #7") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(8)
            .aka("class #8") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(9)
            .aka("class #9") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(10)
            .aka("class #10") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(11)
            .aka("class #11") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(12)
            .aka("class #12") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(13)
            .aka("class #13") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(14)
            .aka("class #14") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(15)
            .aka("class #15") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(16)
            .aka("class #16") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(17)
            .aka("class #17") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(18)
            .aka("class #18") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(19)
            .aka("class #19") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(20)
            .aka("class #20") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(21)
            .aka("class #21") must_=== classOf[java.lang.Short].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "TYPE_CAT")
        .and(meta.getColumnName(2) aka "name #2" must_=== "TYPE_SCHEM")
        .and(meta.getColumnName(3) aka "name #3" must_=== "TYPE_NAME")
        .and(meta.getColumnName(4) aka "name #4" must_=== "ATTR_NAME")
        .and(meta.getColumnName(5) aka "name #5" must_=== "DATA_TYPE")
        .and(meta.getColumnName(6) aka "name #6" must_=== "ATTR_TYPE_NAME")
        .and(meta.getColumnName(7) aka "name #7" must_=== "ATTR_SIZE")
        .and(meta.getColumnName(8) aka "name #8" must_=== "DECIMAL_DIGITS")
        .and(meta.getColumnName(9) aka "name #9" must_=== "NUM_PREC_RADIX")
        .and(meta.getColumnName(10) aka "name #10" must_=== "NULLABLE")
        .and(meta.getColumnName(11) aka "name #11" must_=== "REMARKS")
        .and(meta.getColumnName(12) aka "name #12" must_=== "ATTR_DEF")
        .and(meta.getColumnName(13) aka "name #13" must_=== "SQL_DATA_TYPE")
        .and(meta.getColumnName(14) aka "name #14" must_=== "SQL_DATETIME_SUB")
        .and(
          meta.getColumnName(15).aka("name #15") must_=== "CHAR_OCTET_LENGTH"
        )
        .and(meta.getColumnName(16) aka "name #16" must_=== "ORDINAL_POSITION")
        .and(meta.getColumnName(17) aka "name #17" must_=== "IS_NULLABLE")
        .and(meta.getColumnName(18) aka "name #18" must_=== "SCOPE_CATLOG")
        .and(meta.getColumnName(19) aka "name #19" must_=== "SCOPE_SCHEMA")
        .and(meta.getColumnName(20) aka "name #20" must_=== "SCOPE_TABLE")
        .and(meta.getColumnName(21) aka "name #21" must_=== "SOURCE_DATA_TYPE")

    }

    "be expected one" in {
      (attrs.getFetchSize aka "attributes" must_=== 0)
        .and(attrs.next aka "next attr" must beFalse)

    }
  }

  "Schemas" should {
    lazy val schemas = metadata().getSchemas("catalog", "schema")

    "have expected columns" in {
      lazy val meta = schemas.getMetaData

      (meta.getColumnCount aka "count" must_=== 2)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "TABLE_SCHEM")
        .and(meta.getColumnName(2) aka "name #2" must_=== "TABLE_CATALOG")

    }

    "not be known" in {
      (schemas.getFetchSize aka "schemas" must_=== 0)
        .and(schemas.next aka "next schema" must beFalse)

    }
  }

  "Functions" should {
    lazy val funcs = metadata().getFunctions("catalog", "schema", "func")
    lazy val cols =
      metadata().getFunctionColumns("catalog", "schema", "func", "col")

    "have expected function columns" in {
      lazy val meta = funcs.getMetaData

      (meta.getColumnCount aka "count" must_=== 6)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(5)
            .aka("class #5") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(6)
            .aka("class #6") must_=== classOf[String].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "FUNCTION_CAT")
        .and(meta.getColumnName(2) aka "name #2" must_=== "FUNCTION_SCHEM")
        .and(meta.getColumnName(3) aka "name #3" must_=== "FUNCTION_NAME")
        .and(meta.getColumnName(4) aka "name #4" must_=== "REMARKS")
        .and(meta.getColumnName(5) aka "name #5" must_=== "FUNCTION_TYPE")
        .and(meta.getColumnName(6) aka "name #6" must_=== "SPECIFIC_NAME")

    }

    "not be listed" in {
      (funcs.getFetchSize aka "functions" must_=== 0)
        .and(funcs.next aka "next function" must beFalse)

    }

    "have expected columns" in {
      lazy val meta = cols.getMetaData

      (meta.getColumnCount aka "count" must_=== 17)
        .and(
          meta
            .getColumnClassName(1)
            .aka("class #1") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(2)
            .aka("class #2") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(3)
            .aka("class #3") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(4)
            .aka("class #4") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(5)
            .aka("class #5") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(6)
            .aka("class #6") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(7)
            .aka("class #7") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(8)
            .aka("class #8") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(9)
            .aka("class #9") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(10)
            .aka("class #10") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(11)
            .aka("class #11") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(12)
            .aka("class #12") must_=== classOf[java.lang.Short].getName
        )
        .and(
          meta
            .getColumnClassName(13)
            .aka("class #13") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(14)
            .aka("class #14") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(15)
            .aka("class #15") must_=== classOf[java.lang.Integer].getName
        )
        .and(
          meta
            .getColumnClassName(16)
            .aka("class #16") must_=== classOf[String].getName
        )
        .and(
          meta
            .getColumnClassName(17)
            .aka("class #17") must_=== classOf[String].getName
        )
        .and(meta.getColumnName(1) aka "name #1" must_=== "FUNCTION_CAT")
        .and(meta.getColumnName(2) aka "name #2" must_=== "FUNCTION_SCHEM")
        .and(meta.getColumnName(3) aka "name #3" must_=== "FUNCTION_NAME")
        .and(meta.getColumnName(4) aka "name #4" must_=== "COLUMN_NAME")
        .and(meta.getColumnName(5) aka "name #5" must_=== "COLUMN_TYPE")
        .and(meta.getColumnName(6) aka "name #6" must_=== "DATA_TYPE")
        .and(meta.getColumnName(7) aka "name #7" must_=== "TYPE_NAME")
        .and(meta.getColumnName(8) aka "name #8" must_=== "PRECISION")
        .and(meta.getColumnName(9) aka "name #9" must_=== "LENGTH")
        .and(meta.getColumnName(10) aka "name #10" must_=== "SCALE")
        .and(meta.getColumnName(11) aka "name #11" must_=== "RADIX")
        .and(meta.getColumnName(12) aka "name #12" must_=== "NULLABLE")
        .and(meta.getColumnName(13) aka "name #13" must_=== "REMARKS")
        .and(
          meta.getColumnName(14).aka("name #14") must_=== "CHAR_OCTET_LENGTH"
        )
        .and(meta.getColumnName(15) aka "name #15" must_=== "ORDINAL_POSITION")
        .and(meta.getColumnName(16).aka("name #16") must_=== "IS_NULLABLE")
        .and(meta.getColumnName(17) aka "name #17" must_=== "SPECIFIC_NAME")

    }

    "not be described" in {
      (cols.getFetchSize aka "function cols" must_=== 0)
        .and(cols.next aka "next col" must beFalse)

    }
  }
}

sealed trait MetaDataFixtures {
  val conHandler = EmptyConnectionHandler

  def metadata(
      c: Connection =
        new acolyte.jdbc.Connection("jdbc:acolyte:test", null, conHandler)
    ) = new DatabaseMetaData(c)
}
