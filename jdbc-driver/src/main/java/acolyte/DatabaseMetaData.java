package acolyte;

import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;

/**
 * Acolyte meta-data.
 *
 * @author Cedric Chantepie
 */
public final class DatabaseMetaData implements java.sql.DatabaseMetaData {
    // --- Properties ---

    /**
     * Connection
     */
    private final acolyte.Connection connection;

    // --- Constructors ---

    /**
     * Connection constructor.
     */
    DatabaseMetaData(final acolyte.Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException();
        } // end of if

        this.connection = connection;
    } // end of <init>

    // ---

    /**
     * {@inheritDoc}
     */
    public boolean allProceduresAreCallable() throws SQLException {
        return true;
    } // end of allProceduresAreCallable

    /**
     * {@inheritDoc}
     */
    public boolean allTablesAreSelectable() throws SQLException {
        return true;
    } // end of allTablesAreSelectable

    /**
     * {@inheritDoc}
     */
    public String getURL() throws SQLException {
        return this.connection.url;
    } // end of getURL

    /**
     * {@inheritDoc}
     */
    public String getUserName() throws SQLException {
        return "acolyte";
    } // end of getUserName

    /**
     * {@inheritDoc}
     */
    public boolean isReadOnly() throws SQLException {
        return this.connection.isReadOnly();
    } // end of isReadOnly

    /**
     * {@inheritDoc}
     */
    public boolean nullsAreSortedHigh() throws SQLException {
        return false;
    } // end of nullsAreSortedHigh

    /**
     * {@inheritDoc}
     */
    public boolean nullsAreSortedLow() throws SQLException {
        return true;
    } // end of nullsAreSortedLow

    /**
     * {@inheritDoc}
     */
    public boolean nullsAreSortedAtStart() throws SQLException {
        return false;
    } // end of nullsAreSortedAtStart

    /**
     * {@inheritDoc}
     */
    public boolean nullsAreSortedAtEnd() throws SQLException {
        return true;
    } // end of nullsAreSortedAtEnd

    /**
     * {@inheritDoc}
     */
    public String getDatabaseProductName() throws SQLException {
        return "Acolyte";
    } // end of getDatabaseProductName

    /**
     * {@inheritDoc}
     */
    public String getDatabaseProductVersion() throws SQLException {
        return "0.1-beta";
    } // end of getDatabaseProductVersion

    /**
     * {@inheritDoc}
     */
    public String getDriverName() throws SQLException {
        return "acolyte";
    } // end of getDriverName

    /**
     * {@inheritDoc}
     */
    public String getDriverVersion() throws SQLException {
        return String.format("%d.%d", 
                             getDriverMajorVersion(),
                             getDriverMinorVersion());

    } // end of getDriverVersion

    /**
     * {@inheritDoc}
     */
    public int getDriverMajorVersion() {
        return Driver.MAJOR_VERSION;
    } // end of getDriverMajorVersion

    /**
     * {@inheritDoc}
     */
    public int getDriverMinorVersion() {
        return Driver.MINOR_VERSION;
    } // end of getDriverMinorVersion

    /**
     * {@inheritDoc}
     */
    public boolean usesLocalFiles() throws SQLException {
        return false;
    } // end of usesLocalFiles

    /**
     * {@inheritDoc}
     */
    public boolean usesLocalFilePerTable() throws SQLException {
        return false;
    } // end of usesLocalFilePerTable

    /**
     * {@inheritDoc}
     */
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return true;
    } // end of supportsMixedCaseIdentifiers

    /**
     * {@inheritDoc}
     */
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return false;
    } // end of storesUpperCaseIdentifiers

    /**
     * {@inheritDoc}
     */
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return false;
    } // end of storesLowerCaseIdentifiers

    /**
     * {@inheritDoc}
     */
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return true;
    } // end of storesMixedCaseIdentifiers

    /**
     * {@inheritDoc}
     */
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return true;
    } // end of supportsMixedCaseQuotedIdentifiers

    /**
     * {@inheritDoc}
     */
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return false;
    } // end of storesUpperCaseQuotedIdentifiers

    /**
     * {@inheritDoc}
     */
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return false;
    } // end of storesLowerCaseQuotedIdentifiers

    /**
     * {@inheritDoc}
     */
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return true;
    } // end of storesMixedCaseQuotedIdentifiers

    /**
     * {@inheritDoc}
     */
    public String getIdentifierQuoteString() throws SQLException {
        return "`";
    } // end of getIdentifierQuoteString

    /**
     * {@inheritDoc}
     */
    public String getSQLKeywords() throws SQLException {
        return "";
    } // end of getSQLKeywords

    /**
     * {@inheritDoc}
     */
    public String getNumericFunctions() throws SQLException {
        return "";
    } // end of getNumericFunctions

    /**
     * {@inheritDoc}
     */
    public String getStringFunctions() throws SQLException {
        return "";
    } // end of getStringFunctions

    /**
     * {@inheritDoc}
     */
    public String getSystemFunctions() throws SQLException {
        return "";
    } // end of getSystemFunctions

    /**
     * {@inheritDoc}
     */
    public String getTimeDateFunctions() throws SQLException {
        return "";
    } // end of getTimeDateFunctions

    /**
     * {@inheritDoc}
     */
    public String getSearchStringEscape() throws SQLException {
        return "\\";
    } // end of getSearchStringEscape

    /**
     * {@inheritDoc}
     */
    public String getExtraNameCharacters() throws SQLException {
        return "";
    } // end of getExtraNameCharacters

    /**
     * {@inheritDoc}
     */
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return true;
    } // end of supportsAlterTableWithAddColumn

    /**
     * {@inheritDoc}
     */
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return true;
    } // end of supportsAlterTableWithDropColumn

    /**
     * {@inheritDoc}
     */
    public boolean supportsColumnAliasing() throws SQLException {
        return true;
    } // end of supportsColumnAliasing

    /**
     * {@inheritDoc}
     */
    public boolean nullPlusNonNullIsNull() throws SQLException {
        return true;
    } // end of nullPlusNonNullIsNull

    /**
     * {@inheritDoc}
     */
    public boolean supportsConvert() throws SQLException {
        return false;
    } // end of supportsConvert

    /**
     * {@inheritDoc}
     */
    public boolean supportsConvert(int fromType, int toType) 
        throws SQLException {

        return false;
    } // end of supportsConvert

    /**
     * {@inheritDoc}
     */
    public boolean supportsTableCorrelationNames() throws SQLException {
        return true;
    } // end of supportsTableCorrelationNames

    /**
     * {@inheritDoc}
     */
    public boolean supportsDifferentTableCorrelationNames() 
        throws SQLException {

        return true;
    } // end of supportsDifferentTableCorrelationNames

    /**
     * {@inheritDoc}
     */
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return true;
    } // end of supportsExpressionsInOrderBy

    /**
     * {@inheritDoc}
     */
    public boolean supportsOrderByUnrelated() throws SQLException {
        return true;
    } // end of supportsOrderByUnrelated

    /**
     * {@inheritDoc}
     */
    public boolean supportsGroupBy() throws SQLException {
        return true;
    } // end of supportsGroupBy

    /**
     * {@inheritDoc}
     */
    public boolean supportsGroupByUnrelated() throws SQLException {
        return true;
    } // end of supportsGroupByUnrelated

    /**
     * {@inheritDoc}
     */
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return true;
    } // end of supportsGroupByBeyondSelect

    /**
     * {@inheritDoc}
     */
    public boolean supportsLikeEscapeClause() throws SQLException {
        return true;
    } // end of supportsLikeEscapeClause

    /**
     * {@inheritDoc}
     */
    public boolean supportsMultipleResultSets() throws SQLException {
        return false;
    } // end of supportsMultipleResultSets

    /**
     * {@inheritDoc}
     */
    public boolean supportsMultipleTransactions() throws SQLException {
        return true;
    } // end of supportsMultipleTransactions

    /**
     * {@inheritDoc}
     */
    public boolean supportsNonNullableColumns() throws SQLException {
        return true;
    } // end of supportsNonNullableColumns

    /**
     * {@inheritDoc}
     */
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return true;
    } // end of supportsMinimumSQLGrammar

    /**
     * {@inheritDoc}
     */
    public boolean supportsCoreSQLGrammar() throws SQLException {
        return true;
    } // end of supportsCoreSQLGrammar

    /**
     * {@inheritDoc}
     */
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return true;
    } // end of supportsExtendedSQLGrammar

    /**
     * {@inheritDoc}
     */
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return true;
    } // end of supportsANSI92EntryLevelSQL

    /**
     * {@inheritDoc}
     */
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return true;
    } // end of supportsANSI92IntermediateSQL

    /**
     * {@inheritDoc}
     */
    public boolean supportsANSI92FullSQL() throws SQLException {
        return true;
    } // end of supportsANSI92FullSQL

    /**
     * {@inheritDoc}
     */
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return true;
    } // end of supportsIntegrityEnhancementFacility

    /**
     * {@inheritDoc}
     */
    public boolean supportsOuterJoins() throws SQLException {
        return true;
    } // end of supportsOuterJoins

    /**
     * {@inheritDoc}
     */
    public boolean supportsFullOuterJoins() throws SQLException {
        return true;
    } // end of supportsFullOuterJoins

    /**
     * {@inheritDoc}
     */
    public boolean supportsLimitedOuterJoins() throws SQLException {
        return true;
    } // end of supportsLimitedOuterJoins

    /**
     * {@inheritDoc}
     */
    public String getSchemaTerm() throws SQLException {
        return "schema";
    } // end of getSchemaTerm

    /**
     * {@inheritDoc}
     */
    public String getProcedureTerm() throws SQLException {
        return "procedure";
    } // end of getProcedureTerm

    /**
     * {@inheritDoc}
     */
    public String getCatalogTerm() throws SQLException {
        return "catalog";
    } // end of getCatalogTerm

    /**
     * {@inheritDoc}
     */
    public boolean isCatalogAtStart() throws SQLException {
        return true;
    } // end of isCatalogAtStart

    /**
     * {@inheritDoc}
     */
    public String getCatalogSeparator() throws SQLException {
        return ".";
    } // end of getCatalogSeparator

    /**
     * {@inheritDoc}
     */
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return true;
    } // end of supportsSchemasInDataManipulation

    /**
     * {@inheritDoc}
     */
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return true;
    } // end of supportsSchemasInProcedureCalls

    /**
     * {@inheritDoc}
     */
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return true;
    } // end of supportsSchemasInTableDefinitions

    /**
     * {@inheritDoc}
     */
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return true;
    } // end of supportsSchemasInIndexDefinitions

    /**
     * {@inheritDoc}
     */
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return true;
    } // end of supportsSchemasInPrivilegeDefinitions

    /**
     * {@inheritDoc}
     */
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return true;
    } // end of supportsCatalogsInDataManipulation

    /**
     * {@inheritDoc}
     */
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return true;
    } // end of supportsCatalogsInProcedureCalls

    /**
     * {@inheritDoc}
     */
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return true;
    } // end of supportsCatalogsInTableDefinitions

    /**
     * {@inheritDoc}
     */
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return true;
    } // end of supportsCatalogsInIndexDefinitions

    /**
     * {@inheritDoc}
     */
    public boolean supportsCatalogsInPrivilegeDefinitions() 
        throws SQLException {

        return true;
    } // end of supportsCatalogsInPrivilegeDefinitions

    /**
     * {@inheritDoc}
     */
    public boolean supportsPositionedDelete() throws SQLException {
        return true;
    } // end of supportsPositionedDelete

    /**
     * {@inheritDoc}
     */
    public boolean supportsPositionedUpdate() throws SQLException {
        return true;
    } // end of supportsPositionedUpdate

    /**
     * {@inheritDoc}
     */
    public boolean supportsSelectForUpdate() throws SQLException {
        return true;
    } // end of supportsSelectForUpdate

    /**
     * {@inheritDoc}
     */
    public boolean supportsStoredProcedures() throws SQLException {
        return true;
    } // end of supportsStoredProcedures

    /**
     * {@inheritDoc}
     */
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return true;
    } // end of supportsSubqueriesInComparisons

    /**
     * {@inheritDoc}
     */
    public boolean supportsSubqueriesInExists() throws SQLException {
        return true;
    } // end of supportsSubqueriesInExists

    /**
     * {@inheritDoc}
     */
    public boolean supportsSubqueriesInIns() throws SQLException {
        return true;
    } // end of supportsSubqueriesInIns

    /**
     * {@inheritDoc}
     */
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return true;
    } // end of supportsSubqueriesInQuantifieds

    /**
     * {@inheritDoc}
     */
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return true;
    } // end of supportsCorrelatedSubqueries

    /**
     * {@inheritDoc}
     */
    public boolean supportsUnion() throws SQLException {
        return true;
    } // end of supportsUnion

    /**
     * {@inheritDoc}
     */
    public boolean supportsUnionAll() throws SQLException {
        return true;
    } // end of supportsUnionAll

    /**
     * {@inheritDoc}
     */
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return true;
    } // end of supportsOpenCursorsAcrossCommit

    /**
     * {@inheritDoc}
     */
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return true;
    } // end of supportsOpenCursorsAcrossRollback

    /**
     * {@inheritDoc}
     */
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return true;
    } // end of supportsOpenStatementsAcrossCommit

    /**
     * {@inheritDoc}
     */
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return true;
    } // end of supportsOpenStatementsAcrossRollback

    /**
     * {@inheritDoc}
     */
    public int getMaxBinaryLiteralLength() throws SQLException {
        return 0;
    } // end of getMaxBinaryLiteralLength

    /**
     * {@inheritDoc}
     */
    public int getMaxCharLiteralLength() throws SQLException {
        return 0;
    } // end of getMaxCharLiteralLength

    /**
     * {@inheritDoc}
     */
    public int getMaxColumnNameLength() throws SQLException {
        return 0;
    } // end of getMaxColumnNameLength

    /**
     * {@inheritDoc}
     */
    public int getMaxColumnsInGroupBy() throws SQLException {
        return 0;
    } // end of getMaxColumnsInGroupBy

    /**
     * {@inheritDoc}
     */
    public int getMaxColumnsInIndex() throws SQLException {
        return 0;
    } // end of getMaxColumnsInIndex

    /**
     * {@inheritDoc}
     */
    public int getMaxColumnsInOrderBy() throws SQLException {
        return 0;
    } // end of getMaxColumnsInOrderBy

    /**
     * {@inheritDoc}
     */
    public int getMaxColumnsInSelect() throws SQLException {
        return 0;
    } // end of getMaxColumnsInSelect

    /**
     * {@inheritDoc}
     */
    public int getMaxColumnsInTable() throws SQLException {
        return 0;
    } // end of getMaxColumnsInTable

    /**
     * {@inheritDoc}
     */
    public int getMaxConnections() throws SQLException {
        return 0;
    } // end of getMaxConnections

    /**
     * {@inheritDoc}
     */
    public int getMaxCursorNameLength() throws SQLException {
        return 0;
    } // end of getMaxCursorNameLength

    /**
     * {@inheritDoc}
     */
    public int getMaxIndexLength() throws SQLException {
        return 0;
    } // end of getMaxIndexLength

    /**
     * {@inheritDoc}
     */
    public int getMaxSchemaNameLength() throws SQLException {
        return 0;
    } // end of getMaxSchemaNameLength

    /**
     * {@inheritDoc}
     */
    public int getMaxProcedureNameLength() throws SQLException {
        return 0;
    } // end of getMaxProcedureNameLength

    /**
     * {@inheritDoc}
     */
    public int getMaxCatalogNameLength() throws SQLException {
        return 0;
    } // end of getMaxCatalogNameLength

    /**
     * {@inheritDoc}
     */
    public int getMaxRowSize() throws SQLException {
        return 0;
    } // end of getMaxRowSize

    /**
     * {@inheritDoc}
     */
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return true;
    } // end of doesMaxRowSizeIncludeBlobs

    /**
     * {@inheritDoc}
     */
    public int getMaxStatementLength() throws SQLException {
        return 0;
    } // end of getMaxStatementLength

    /**
     * {@inheritDoc}
     */
    public int getMaxStatements() throws SQLException {
        return 0;
    } // end of getMaxStatements

    /**
     * {@inheritDoc}
     */
    public int getMaxTableNameLength() throws SQLException {
        return 0;
    } // end of getMaxTableNameLength

    /**
     * {@inheritDoc}
     */
    public int getMaxTablesInSelect() throws SQLException {
        return 0;
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxUserNameLength() throws SQLException {
        return 0;
    } // end of getMaxUserNameLength

    /**
     * {@inheritDoc}
     */
    public int getDefaultTransactionIsolation() throws SQLException {
        return Connection.TRANSACTION_NONE;
    } // end of getDefaultTransactionIsolation

    /**
     * {@inheritDoc}
     */
    public boolean supportsTransactions() throws SQLException {
        return true;
    } // end of supportsTransaction

    /**
     * {@inheritDoc}
     */
    public boolean supportsTransactionIsolationLevel(int level) 
        throws SQLException {

        return true;
    } // end of supportsTransactionIsolationLevel

    /**
     * {@inheritDoc}
     */
    public boolean supportsDataDefinitionAndDataManipulationTransactions() 
        throws SQLException {

        return true;
    } // end of supportsDataDefinitionAndDataManipulationTransactions

    /**
     * {@inheritDoc}
     */
    public boolean supportsDataManipulationTransactionsOnly() 
        throws SQLException {

        return false;
    } // end of supportsDataManipulationTransactionsOnly

    /**
     * {@inheritDoc}
     */
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return false;
    } // end of dataDefinitionCausesTransactionCommit

    /**
     * {@inheritDoc}
     */
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return false;
    } // end of dataDefinitionIgnoredInTransactions

    /**
     * {@inheritDoc}
     */
    public ResultSet getProcedures(final String catalog, 
                                   final String schemaPatterns, 
                                   final String procedureNamePattern) 
        throws SQLException {

        return RowLists.rowList8(String.class, String.class, String.class,
                                 Object.class, Object.class, Object.class,
                                 String.class, Short.class).
            withLabel(1, "PROCEDURE_CAT").
            withLabel(2, "PROCEDURE_SCHEM").
            withLabel(3, "PROCEDURE_NAME").
            withLabel(7, "REMARKS").
            withLabel(8, "PROCEDURE_TYPE").
            resultSet();

    } // end of getProcedures

    /**
     * {@inheritDoc}
     */
    public ResultSet getProcedureColumns(final String catalog, 
                                         final String schemaPatterns, 
                                         final String procedureNamePattern,
                                         final String columnNamePattern)
        throws SQLException {

        return RowLists.rowList13(String.class, String.class, String.class,
                                  String.class, Short.class, Integer.class,
                                  String.class, Integer.class, Integer.class,
                                  Short.class, Short.class, Short.class, 
                                  String.class).
            withLabel(1, "PROCEDURE_CAT").
            withLabel(2, "PROCEDURE_SCHEM").
            withLabel(3, "PROCEDURE_NAME").
            withLabel(4, "COLUMN_NAME").
            withLabel(5, "COLUMN_TYPE").
            withLabel(6, "DATA_TYPE").
            withLabel(7, "TYPE_NAME").
            withLabel(8, "PRECISION").
            withLabel(9, "LENGTH").
            withLabel(10, "SCALE").
            withLabel(11, "RADIX").
            withLabel(12, "NULLABLE").
            withLabel(13, "REMARKS").
            resultSet();

    } // end of getProcedureColumns

    /**
     * {@inheritDoc}
     */
    public ResultSet getTables(final String catalog, 
                               final String schemaPatterns, 
                               final String tableNamePattern, 
                               final String[] types) throws SQLException {

        return RowLists.rowList10(String.class, String.class, String.class, 
                                  String.class, String.class, String.class,
                                  String.class, String.class, String.class,
                                  String.class).
            withLabel(1, "TABLE_CAT").
            withLabel(2, "TABLE_SCHEM").
            withLabel(3, "TABLE_NAME").
            withLabel(4, "TABLE_TYPE").
            withLabel(5, "REMARKS").
            withLabel(6, "TYPE_CAT").
            withLabel(7, "TYPE_SCHEM").
            withLabel(8, "TYPE_NAME").
            withLabel(9, "SELF_REFERENCING_COL_NAME").
            withLabel(10, "REF_GENERATION").
            resultSet();

    } // end of getTables

    /**
     * {@inheritDoc}
     */
    public ResultSet getSchemas() throws SQLException {
        return RowLists.rowList2(String.class, String.class).
            withLabel(1, "TABLE_SCHEM").
            withLabel(2, "TABLE_CATALOG").
            resultSet();

    } // end of getSchemas

    /**
     * {@inheritDoc}
     */
    public ResultSet getCatalogs() throws SQLException {
        return RowLists.rowList1(String.class).
            withLabel(1, "TABLE_CAT").resultSet();

    } // end of getCatalogs

    /**
     * {@inheritDoc}
     */
    public ResultSet getTableTypes() throws SQLException {
        return RowLists.rowList1(String.class).
            withLabel(1, "TABLE_TYPE").resultSet();

    } // end of getTableTypes

    /**
     * {@inheritDoc}
     */
    public ResultSet getColumns(final String catalog,
                                final String schemaPattern,
                                final String tableNamePattern,
                                final String columnNamePattern)
        throws SQLException {

        return RowLists.rowList21(String.class, String.class, String.class,
                                  String.class, Integer.class, String.class,
                                  Integer.class, Integer.class, Integer.class,
                                  Integer.class, String.class, String.class,
                                  Integer.class, Integer.class, Integer.class, 
                                  Integer.class, String.class, String.class, 
                                  String.class, String.class, Short.class).
            withLabel(1, "TABLE_CAT").
            withLabel(2, "TABLE_SCHEM").
            withLabel(3, "TABLE_NAME").
            withLabel(4, "COLUMN_NAME").
            withLabel(5, "DATA_TYPE").
            withLabel(6, "TYPE_NAME").
            withLabel(7, "BUFFER_LENGTH").
            withLabel(8, "DECIMAL_DIGITS").
            withLabel(9, "NUM_PREC_RADIX").
            withLabel(10, "NULLABLE").
            withLabel(11, "REMARKS").
            withLabel(12, "COLUMN_DEF").
            withLabel(13, "SQL_DATA_TYPE").
            withLabel(14, "SQL_DATETIME_SUB").
            withLabel(15, "CHAR_OCTET_LENGTH").
            withLabel(16, "ORDINAL_POSITION").
            withLabel(17, "IS_NULLABLE").
            withLabel(18, "SCOPE_CATLOG").
            withLabel(19, "SCOPE_SCHEMA").
            withLabel(20, "SCOPE_TABLE").
            withLabel(21, "SOURCE_DATA_TYPE").
            resultSet();

    } // end of getColumns

    /**
     * {@inheritDoc}
     */
    public ResultSet getColumnPrivileges(final String catalog,
                                         final String schema,
                                         final String table,
                                         final String columnNamePattern)
        throws SQLException {

        return RowLists.rowList8(String.class, String.class, String.class,
                                 String.class, String.class, String.class,
                                 String.class, String.class).
            withLabel(1, "TABLE_CAT").
            withLabel(2, "TABLE_SCHEM").
            withLabel(3, "TABLE_NAME").
            withLabel(4, "COLUMN_NAME").
            withLabel(5, "GRANTOR").
            withLabel(6, "GRANTEE").
            withLabel(7, "PRIVILEGE").
            withLabel(8, "IS_GRANTABLE").
            resultSet();

    } // end of getColumnPrivileges

    /**
     * {@inheritDoc}
     */
    public ResultSet getTablePrivileges(final String catalog,
                                        final String schemaPattern,
                                        final String tableNamePattern) 
        throws SQLException {

        return RowLists.rowList7(String.class, String.class, String.class,
                                 String.class, String.class, String.class,
                                 String.class).
            withLabel(1, "TABLE_CAT").
            withLabel(2, "TABLE_SCHEM").
            withLabel(3, "TABLE_NAME").
            withLabel(4, "GRANTOR").
            withLabel(5, "GRANTEE").
            withLabel(6, "PRIVILEGE").
            withLabel(7, "IS_GRANTABLE").
            resultSet();

    } // end of getTablePrivileges

    /**
     * {@inheritDoc}
     */
    public ResultSet getBestRowIdentifier(final String catalog,
                                          final String schema,
                                          final String table,
                                          final int scope,
                                          final boolean nullable)
        throws SQLException {

        return RowLists.rowList8(Short.class, String.class, Integer.class,
                                 String.class, Integer.class, Integer.class,
                                 Short.class, Short.class).
            withLabel(1, "SCOPE").
            withLabel(2, "COLUMN_NAME").
            withLabel(3, "DATA_TYPE").
            withLabel(4, "TYPE_NAME").
            withLabel(5, "COLUMN_SIZE").
            withLabel(6, "BUFFER_LENGTH").
            withLabel(7, "DECIMAL_DIGITS").
            withLabel(8, "PSEUDO_COLUMN").
            resultSet();

    } // end of getBestRowIdentifier

    /**
     * {@inheritDoc}
     */
    public ResultSet getVersionColumns(final String catalog,
                                       final String schema,
                                       final String table)
        throws SQLException {

        return RowLists.rowList8(Short.class, String.class, Integer.class,
                                 String.class, Integer.class, Integer.class,
                                 Short.class, Short.class).
            withLabel(1, "SCOPE").
            withLabel(2, "COLUMN_NAME").
            withLabel(3, "DATA_TYPE").
            withLabel(4, "TYPE_NAME").
            withLabel(5, "COLUMN_SIZE").
            withLabel(6, "BUFFER_LENGTH").
            withLabel(7, "DECIMAL_DIGITS").
            withLabel(8, "PSEUDO_COLUMN").
            resultSet();

    } // end of getVersionColumns

    /**
     * {@inheritDoc}
     */
    public ResultSet getPrimaryKeys(final String catalog,
                                    final String schema,
                                    final String table)
        throws SQLException {

        return RowLists.rowList6(String.class, String.class, String.class, 
                                 String.class, Short.class, String.class).
            withLabel(1, "TABLE_CAT").
            withLabel(2, "TABLE_SCHEM").
            withLabel(3, "TABLE_NAME").
            withLabel(4, "COLUMN_NAME").
            withLabel(5, "KEY_SEQ").
            withLabel(6, "PK_NAME").
            resultSet();

    } // end of getPrimaryKey

    /**
     * {@inheritDoc}
     */
    public ResultSet getImportedKeys(final String catalog,
                                     final String schema,
                                     final String table)
        throws SQLException {

        return RowLists.rowList14(String.class, String.class, String.class, 
                                  String.class, String.class, String.class, 
                                  String.class, String.class, Short.class, 
                                  Short.class, Short.class, String.class, 
                                  String.class, Short.class).
            withLabel(1, "PKTABLE_CAT").
            withLabel(2, "PKTABLE_SCHEM").
            withLabel(3, "PKTABLE_NAME").
            withLabel(4, "PKCOLUMN_NAME").
            withLabel(5, "FKTABLE_CAT").
            withLabel(6, "FKTABLE_SCHEM").
            withLabel(7, "FKTABLE_NAME").
            withLabel(8, "FKCOLUMN_NAME").
            withLabel(9, "KEY_SEQ").
            withLabel(10, "UPDATE_RULE").
            withLabel(11, "DELETE_RULE").
            withLabel(12, "FK_NAME").
            withLabel(13, "PK_NAME").
            withLabel(14, "DEFERRABILITY").
            resultSet();
            
    } // end of getImportedKeys

    /**
     * {@inheritDoc}
     */
    public ResultSet getExportedKeys(final String catalog,
                                     final String schema,
                                     final String table)
        throws SQLException {

        return RowLists.rowList14(String.class, String.class, String.class, 
                                  String.class, String.class, String.class, 
                                  String.class, String.class, Short.class, 
                                  Short.class, Short.class, String.class, 
                                  String.class, Short.class).
            withLabel(1, "PKTABLE_CAT").
            withLabel(2, "PKTABLE_SCHEM").
            withLabel(3, "PKTABLE_NAME").
            withLabel(4, "PKCOLUMN_NAME").
            withLabel(5, "FKTABLE_CAT").
            withLabel(6, "FKTABLE_SCHEM").
            withLabel(7, "FKTABLE_NAME").
            withLabel(8, "FKCOLUMN_NAME").
            withLabel(9, "KEY_SEQ").
            withLabel(10, "UPDATE_RULE").
            withLabel(11, "DELETE_RULE").
            withLabel(12, "FK_NAME").
            withLabel(13, "PK_NAME").
            withLabel(14, "DEFERRABILITY").
            resultSet();

    } // end of getExportedKeys

    /**
     * {@inheritDoc}
     */
    public ResultSet getCrossReference(final String parentCatalog,
                                       final String parentSchema,
                                       final String parentTable,
                                       final String foreignCatalog,
                                       final String foreignSchema,
                                       final String foreignTable)
        throws SQLException {

        return RowLists.rowList14(String.class, String.class, String.class, 
                                  String.class, String.class, String.class, 
                                  String.class, String.class, Short.class, 
                                  Short.class, Short.class, String.class, 
                                  String.class, Short.class).
            withLabel(1, "PKTABLE_CAT").
            withLabel(2, "PKTABLE_SCHEM").
            withLabel(3, "PKTABLE_NAME").
            withLabel(4, "PKCOLUMN_NAME").
            withLabel(5, "FKTABLE_CAT").
            withLabel(6, "FKTABLE_SCHEM").
            withLabel(7, "FKTABLE_NAME").
            withLabel(8, "FKCOLUMN_NAME").
            withLabel(9, "KEY_SEQ").
            withLabel(10, "UPDATE_RULE").
            withLabel(11, "DELETE_RULE").
            withLabel(12, "FK_NAME").
            withLabel(13, "PK_NAME").
            withLabel(14, "DEFERRABILITY").
            resultSet();

    } // end of getCrossReference

    /**
     * {@inheritDoc}
     */
    public ResultSet getTypeInfo() throws SQLException {
        return RowLists.rowList18(String.class, Integer.class, Integer.class,
                                  String.class, String.class, String.class,
                                  Short.class, Boolean.class, Short.class,
                                  Boolean.class, Boolean.class, Boolean.class,
                                  String.class, Short.class, Short.class, 
                                  Integer.class, Integer.class, Integer.class).
            withLabel(1, "TYPE_NAME").
            withLabel(2, "DATA_TYPE").
            withLabel(3, "PRECISION").
            withLabel(4, "LITERAL_PREFIX").
            withLabel(5, "LITERAL_SUFFIX").
            withLabel(6, "CREATE_PARAMS").
            withLabel(7, "NULLABLE").
            withLabel(8, "CASE_SENSITIVE").
            withLabel(9, "SEARCHABLE").
            withLabel(10, "UNSIGNED_ATTRIBUTE").
            withLabel(11, "FIXED_PREC_SCALE").
            withLabel(12, "AUTO_INCREMENT").
            withLabel(13, "LOCAL_TYPE_NAME").
            withLabel(14, "MINIMUM_SCALE").
            withLabel(15, "MAXIMUM_SCALE").
            withLabel(16, "SQL_DATA_TYPE").
            withLabel(17, "SQL_DATETIME_SUB").
            withLabel(18, "NUM_PREC_RADIX").
            resultSet();

    } // end of getTypeInfo

    /**
     * {@inheritDoc}
     */
    public ResultSet getIndexInfo(final String catalog,
                                  final String schema,
                                  final String table,
                                  final boolean unique,
                                  final boolean approximate)
        throws SQLException {

        return RowLists.rowList13(String.class, String.class, String.class, 
                                  Boolean.class, String.class, String.class, 
                                  Short.class, Short.class, String.class, 
                                  String.class, Integer.class, Integer.class, 
                                  String.class).
            withLabel(1, "TABLE_CAT").
            withLabel(2, "TABLE_SCHEM").
            withLabel(3, "TABLE_NAME").
            withLabel(4, "NON_UNIQUE").
            withLabel(5, "INDEX_QUALIFIER").
            withLabel(6, "INDEX_NAME").
            withLabel(7, "TYPE").
            withLabel(8, "ORDINAL_POSITION").
            withLabel(9, "COLUMN_NAME").
            withLabel(10, "ASC_OR_DESC").
            withLabel(11, "CARDINALITY").
            withLabel(12, "PAGES").
            withLabel(13, "FILTER_CONDITION").
            resultSet();

    } // end of getIndexInfo

    /**
     * {@inheritDoc}
     */
    public boolean supportsResultSetType(final int type) throws SQLException {
        return (type == ResultSet.TYPE_FORWARD_ONLY);
    } // end of supportsResultSetType

    /**
     * {@inheritDoc}
     */
    public boolean supportsResultSetConcurrency(final int type, 
                                                final int concurrency) 
        throws SQLException {

        return supportsResultSetType(type);
    } // end of supportsResultSetConcurrency

    /**
     * {@inheritDoc}
     */
    public boolean ownUpdatesAreVisible(final int type) throws SQLException {
        return supportsResultSetType(type);
    } // end of ownUpdatesAreVisible

    /**
     * {@inheritDoc}
     */
    public boolean ownDeletesAreVisible(final int type) throws SQLException {
        return supportsResultSetType(type);
    } // end of ownDeletesAreVisible

    /**
     * {@inheritDoc}
     */
    public boolean ownInsertsAreVisible(final int type) throws SQLException {
        return supportsResultSetType(type);
    } // end of ownInsertsAreVisible

    /**
     * {@inheritDoc}
     */
    public boolean othersUpdatesAreVisible(final int type) throws SQLException {
        return false;
    } // end of othersUpdatesAreVisible

    /**
     * {@inheritDoc}
     */
    public boolean othersDeletesAreVisible(final int type) throws SQLException {
        return false;
    } // end of othersDeletesAreVisible

    /**
     * {@inheritDoc}
     */
    public boolean othersInsertsAreVisible(final int type) throws SQLException {
        return false;
    } // end of othersInsertsAreVisible

    /**
     * {@inheritDoc}
     */
    public boolean updatesAreDetected(final int type) throws SQLException {
        return supportsResultSetType(type);
    } // end of updatesAreDetected

    /**
     * {@inheritDoc}
     */
    public boolean deletesAreDetected(final int type) throws SQLException {
        return supportsResultSetType(type);
    } // end of deletesAreDetected

    /**
     * {@inheritDoc}
     */
    public boolean insertsAreDetected(final int type) throws SQLException {
        return supportsResultSetType(type);
    } // end of insertsAreDetected

    /**
     * {@inheritDoc}
     */
    public boolean supportsBatchUpdates() throws SQLException {
        return true;
    } // end of supportsBatchUpdates

    /**
     * {@inheritDoc}
     */
    public ResultSet getUDTs(String catalog,
                             String schemaPattern,
                             String typeNamePattern,
                             int[] types)
        throws SQLException {

        return RowLists.rowList7(String.class, String.class, String.class,
                                 String.class, Integer.class, String.class,
                                 Short.class).
            withLabel(1, "TYPE_CAT").
            withLabel(2, "TYPE_SCHEM").
            withLabel(3, "TYPE_NAME").
            withLabel(4, "CLASS_NAME").
            withLabel(5, "DATA_TYPE").
            withLabel(6, "REMARKS").
            withLabel(7, "BASE_TYPE").
            resultSet();

    } // end of getUDTs

    /**
     * {@inheritDoc}
     */
    public Connection getConnection() throws SQLException {
        return this.connection;
    } // end of getConnection

    /**
     * {@inheritDoc}
     */
    public boolean supportsSavepoints() throws SQLException {
        return false;
    } // end of supportsSavepoints

    /**
     * {@inheritDoc}
     */
    public boolean supportsNamedParameters() throws SQLException {
        return true;
    } // end of supportsNamedParameters

    /**
     * {@inheritDoc}
     */
    public boolean supportsMultipleOpenResults() throws SQLException {
        return false;
    } // end of supportsMultipleOpenResults

    /**
     * {@inheritDoc}
     */
    public boolean supportsGetGeneratedKeys() throws SQLException {
        return true;
    } // end of supportsGetGeneratedKeys

    /**
     * {@inheritDoc}
     */
    public ResultSet getSuperTypes(final String catalog,
                                   final String schemaPattern,
                                   final String typeNamePattern)
        throws SQLException {

        return RowLists.rowList6(String.class, String.class, String.class, 
                                 String.class, String.class, String.class).
            withLabel(1, "TYPE_CAT").
            withLabel(2, "TYPE_SCHEM").
            withLabel(3, "TYPE_NAME").
            withLabel(4, "SUPERTYPE_CAT").
            withLabel(5, "SUPERTYPE_SCHEM").
            withLabel(6, "SUPERTYPE_NAME").
            resultSet();

    } // end of getSuperTypes

    /**
     * {@inheritDoc}
     */
    public ResultSet getSuperTables(final String catalog,
                                    final String schemaPattern,
                                    final String tableNamePattern)
        throws SQLException {

        return RowLists.rowList4(String.class, String.class,
                                 String.class, String.class).
            withLabel(1, "TABLE_CAT").
            withLabel(2, "TABLE_SCHEM").
            withLabel(3, "TABLE_NAME").
            withLabel(4, "SUPERTABLE_NAME").
            resultSet();

    } // end of getSuperTables

    /**
     * {@inheritDoc}
     */
    public ResultSet getAttributes(final String catalog,
                                   final String schemaPattern,
                                   final String typeNamePattern,
                                   final String attributeNamePattern)
        throws SQLException {

        return RowLists.rowList21(String.class, String.class, String.class,
                                  String.class, Integer.class, String.class,
                                  Integer.class, Integer.class, Integer.class,
                                  Integer.class, String.class, String.class,
                                  Integer.class, Integer.class, Integer.class, 
                                  Integer.class, String.class, String.class, 
                                  String.class, String.class, Short.class).
            withLabel(1, "TYPE_CAT").
            withLabel(2, "TYPE_SCHEM").
            withLabel(3, "TYPE_NAME").
            withLabel(4, "ATTR_NAME").
            withLabel(5, "DATA_TYPE").
            withLabel(6, "ATTR_TYPE_NAME").
            withLabel(7, "ATTR_SIZE").
            withLabel(8, "DECIMAL_DIGITS").
            withLabel(9, "NUM_PREC_RADIX").
            withLabel(10, "NULLABLE").
            withLabel(11, "REMARKS").
            withLabel(12, "ATTR_DEF").
            withLabel(13, "SQL_DATA_TYPE").
            withLabel(14, "SQL_DATETIME_SUB").
            withLabel(15, "CHAR_OCTET_LENGTH").
            withLabel(16, "ORDINAL_POSITION").
            withLabel(17, "IS_NULLABLE").
            withLabel(18, "SCOPE_CATLOG").
            withLabel(19, "SCOPE_SCHEMA").
            withLabel(20, "SCOPE_TABLE").
            withLabel(21, "SOURCE_DATA_TYPE").
            resultSet();

    } // end of getAttributes

    /**
     * {@inheritDoc}
     */
    public boolean supportsResultSetHoldability(final int holdability) 
        throws SQLException {

        return (holdability == ResultSet.CLOSE_CURSORS_AT_COMMIT);
    } // end of supportsResultSetHoldability

    /**
     * {@inheritDoc}
     */
    public int getResultSetHoldability() throws SQLException {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    } // end of getResultSetHoldability

    /**
     * {@inheritDoc}
     */
    public int getDatabaseMajorVersion() throws SQLException {
        return getDriverMajorVersion();
    } // end of getDatabaseMajorVersion

    /**
     * {@inheritDoc}
     */
    public int getDatabaseMinorVersion() throws SQLException {
        return getDriverMinorVersion();
    } // end of getDatabaseMinorVersion

    /**
     * {@inheritDoc}
     */
    public int getJDBCMajorVersion() throws SQLException {
        return 4;
    } // end of getJDBCMajorVersion

    /**
     * {@inheritDoc}
     */
    public int getJDBCMinorVersion() throws SQLException {
        return 0;
    } // end of getJDBCMinorVersion

    /**
     * {@inheritDoc}
     */
    public int getSQLStateType() throws SQLException {
        return sqlStateXOpen;
    } // end of getSQLStateType

    /**
     * {@inheritDoc}
     */
    public boolean locatorsUpdateCopy() throws SQLException {
        return false;
    } // end of locatorsUpdateCopy

    /**
     * {@inheritDoc}
     */
    public boolean supportsStatementPooling() throws SQLException {
        return false;
    } // end of supportsStatementPooling

    /**
     * {@inheritDoc}
     */
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        return RowIdLifetime.ROWID_UNSUPPORTED;
    } // end of getRowIdLifetime

    /**
     * {@inheritDoc}
     */
    public ResultSet getSchemas(final String catalog, 
                                final String schemaPattern) 
        throws SQLException {

        return RowLists.rowList2(String.class, String.class).
            withLabel(1, "TABLE_SCHEM").
            withLabel(2, "TABLE_CATALOG").
            resultSet();

    } // end of getSchemas

    /**
     * {@inheritDoc}
     */
    public boolean supportsStoredFunctionsUsingCallSyntax() 
        throws SQLException {

        return true;
    } // end of supportsStoredFunctionsUsingCallSyntax

    /**
     * {@inheritDoc}
     */
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return false;
    } // end of autoCommitFailureClosesAllResultSets

    /**
     * {@inheritDoc}
     */
    public ResultSet getClientInfoProperties() throws SQLException {
        return RowLists.rowList4(String.class, Integer.class,
                                 String.class, String.class).
            withLabel(1, "NAME").
            withLabel(2, "MAX_LEN").
            withLabel(3, "DEFAULT_VALUE").
            withLabel(4, "DESCRIPTION").
            resultSet();

    } // end of getClientInfoProperties

    /**
     * {@inheritDoc}
     */
    public ResultSet getFunctions(final String catalog,
                                  final String schemaPattern,
                                  final String functionNamePattern) 
        throws SQLException {

        return RowLists.rowList6(String.class, String.class, String.class, 
                                 String.class, Short.class, String.class).
            withLabel(1, "FUNCTION_CAT").
            withLabel(2, "FUNCTION_SCHEM").
            withLabel(3, "FUNCTION_NAME").
            withLabel(4, "REMARKS").
            withLabel(5, "FUNCTION_TYPE").
            withLabel(6, "SPECIFIC_NAME").
            resultSet();

    } // end of getFunctions

    /**
     * {@inheritDoc}
     */
    public ResultSet getFunctionColumns(final String catalog,
                                        final String schemaPattern,
                                        final String functionNamePattern,
                                        final String columnNamePattern)
        throws SQLException {

        return RowLists.rowList17(String.class, String.class, String.class,
                                  String.class, Short.class, Integer.class,
                                  String.class, Integer.class, Integer.class,
                                  Short.class, Short.class, Short.class,
                                  String.class, Integer.class, Integer.class, 
                                  String.class, String.class).
            withLabel(1, "FUNCTION_CAT").
            withLabel(2, "FUNCTION_SCHEM").
            withLabel(3, "FUNCTION_NAME").
            withLabel(4, "COLUMN_NAME").
            withLabel(5, "COLUMN_TYPE").
            withLabel(6, "DATA_TYPE").
            withLabel(7, "TYPE_NAME").
            withLabel(8, "PRECISION").
            withLabel(9, "LENGTH").
            withLabel(10, "SCALE").
            withLabel(11, "RADIX").
            withLabel(12, "NULLABLE").
            withLabel(13, "REMARKS").
            withLabel(14, "CHAR_OCTET_LENGTH").
            withLabel(15, "ORDINAL_POSITION").
            withLabel(16, "IS_NULLABLE").
            withLabel(17, "SPECIFIC_NAME").
            resultSet();

    } // end of getFunctionColumns

    /**
     * {@inheritDoc}
     */
    public ResultSet getPseudoColumns(final String catalog,
                                      final String schemaPattern,
                                      final String tableNamePattern,
                                      final String columnNamePattern)
        throws SQLException {

        return RowLists.rowList8(String.class, String.class, String.class,
                                 String.class, String.class, String.class,
                                 String.class, String.class).
            withLabel(1, "TABLE_CAT").
            withLabel(2, "TABLE_SCHEM").
            withLabel(3, "TABLE_NAME").
            withLabel(4, "COLUMN_NAME").
            withLabel(5, "GRANTOR").
            withLabel(6, "GRANTEE").
            withLabel(7, "PRIVILEGE").
            withLabel(8, "IS_GRANTABLE").
            resultSet();

    } // end of getPseudoColumns

    /**
     * {@inheritDoc}
     */
    public boolean generatedKeyAlwaysReturned() throws SQLException {
        return false;
    } // end of generatedKeyAlwaysReturned

    /**
     * {@inheritDoc}
     */
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(this.getClass());
    } // end of isWrapperFor

    /**
     * {@inheritDoc}
     */
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface)) {
            throw new SQLException();
        } // end of if

        @SuppressWarnings("unchecked")
        final T proxy = (T) this;

        return proxy;
    } // end of unwrap
} // end of class DatabaseMetaData
