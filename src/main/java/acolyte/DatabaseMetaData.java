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
        return true;
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

        return AbstractResultSet.EMPTY;
    } // end of getProcedures

    /**
     * {@inheritDoc}
     */
    public ResultSet getProcedureColumns(final String catalog, 
                                         final String schemaPatterns, 
                                         final String procedureNamePattern,
                                         final String columnNamePattern)
        throws SQLException {

        return AbstractResultSet.EMPTY;
    } // end of getProcedureColumns

    /**
     * {@inheritDoc}
     */
    public ResultSet getTables(final String catalog, 
                               final String schemaPatterns, 
                               final String tableNamePattern, 
                               final String[] types) throws SQLException {

        return AbstractResultSet.EMPTY;
    } // end of getTables

    /**
     * {@inheritDoc}
     */
    public ResultSet getSchemas() throws SQLException {
        return AbstractResultSet.EMPTY;
    } // end of getSchemas

    /**
     * {@inheritDoc}
     */
    public ResultSet getCatalogs() throws SQLException {
        return AbstractResultSet.EMPTY;
    } // end of getCatalogs

    /**
     * {@inheritDoc}
     */
    public ResultSet getTableTypes() throws SQLException {
        return AbstractResultSet.EMPTY;
    } // end of getTableTypes

    /**
     * {@inheritDoc}
     */
    public ResultSet getColumns(final String catalog,
                                final String schemaPattern,
                                final String tableNamePattern,
                                final String columnNamePattern)
        throws SQLException {

        return AbstractResultSet.EMPTY;
    } // end of getColumns

    /**
     * {@inheritDoc}
     */
    public ResultSet getColumnPrivileges(final String catalog,
                                         final String schema,
                                         final String table,
                                         final String columnNamePattern)
        throws SQLException {

        return AbstractResultSet.EMPTY;
    } // end of getColumnPrivileges

    /**
     * {@inheritDoc}
     */
    public ResultSet getTablePrivileges(final String catalog,
                                        final String schemaPattern,
                                        final String tableNamePattern) 
        throws SQLException {

        return AbstractResultSet.EMPTY;
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

        return AbstractResultSet.EMPTY;
    } // end of getBestRowIdentifier

    /**
     * {@inheritDoc}
     */
    public ResultSet getVersionColumns(final String catalog,
                                       final String schema,
                                       final String table)
        throws SQLException {

        return AbstractResultSet.EMPTY;
    } // end of getVersionColumns

    /**
     * {@inheritDoc}
     */
    public ResultSet getPrimaryKeys(final String catalog,
                                    final String schema,
                                    final String table)
        throws SQLException {

        return AbstractResultSet.EMPTY;
    } // end of getPrimaryKey

    /**
     * {@inheritDoc}
     */
    public ResultSet getImportedKeys(final String catalog,
                                     final String schema,
                                     final String table)
        throws SQLException {

        return AbstractResultSet.EMPTY;
    } // end of getImportedKeys

    /**
     * {@inheritDoc}
     */
    public ResultSet getExportedKeys(final String catalog,
                                     final String schema,
                                     final String table)
        throws SQLException {

        return AbstractResultSet.EMPTY;
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

        return AbstractResultSet.EMPTY;
    } // end of getCrossReference

    /**
     * {@inheritDoc}
     */
    public ResultSet getTypeInfo() throws SQLException {
        return AbstractResultSet.EMPTY;
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

        return AbstractResultSet.EMPTY;
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

        return AbstractResultSet.EMPTY;
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

        return AbstractResultSet.EMPTY;
    } // end of getSuperTypes

    /**
     * {@inheritDoc}
     */
    public ResultSet getSuperTables(final String catalog,
                                    final String schemaPattern,
                                    final String tableNamePattern)
        throws SQLException {

        return AbstractResultSet.EMPTY;
    } // end of getSuperTables

    /**
     * {@inheritDoc}
     */
    public ResultSet getAttributes(final String catalog,
                                   final String schemaPattern,
                                   final String typeNamePattern,
                                   final String attributeNamePattern)
        throws SQLException {

        return AbstractResultSet.EMPTY;
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

        throw new RuntimeException("Not yet implemented");
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
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getFunctions(final String catalog,
                                  final String schemaPattern,
                                  final String functionNamePattern) 
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of getFunctions

    /**
     * {@inheritDoc}
     */
    public ResultSet getFunctionColumns(final String catalog,
                                        final String schemaPattern,
                                        final String functionNamePattern,
                                        final String columnNamePattern)
        throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getPseudoColumns(final String catalog,
                                      final String schemaPattern,
                                      final String tableNamePattern,
                                      final String columnNamePattern)
        throws SQLException {
        throw new RuntimeException("Not yet implemented");
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
