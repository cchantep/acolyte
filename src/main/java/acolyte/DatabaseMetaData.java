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
    private final Connection connection;

    // --- Constructors ---

    /**
     * Connection constructor.
     */
    DatabaseMetaData(final Connection connection) {
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
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean allTablesAreSelectable() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public String getURL() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public String getUserName() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean isReadOnly() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean nullsAreSortedHigh() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean nullsAreSortedLow() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean nullsAreSortedAtStart() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean nullsAreSortedAtEnd() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public String getDatabaseProductName() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public String getDatabaseProductVersion() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public String getDriverName() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public String getDriverVersion() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

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
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean usesLocalFilePerTable() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public String getIdentifierQuoteString() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public String getSQLKeywords() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public String getNumericFunctions() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public String getStringFunctions() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public String getSystemFunctions() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public String getTimeDateFunctions() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public String getSearchStringEscape() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public String getExtraNameCharacters() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsColumnAliasing() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean nullPlusNonNullIsNull() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

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
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsDifferentTableCorrelationNames() 
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsOrderByUnrelated() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsGroupBy() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsGroupByUnrelated() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsLikeEscapeClause() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsMultipleResultSets() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsMultipleTransactions() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsNonNullableColumns() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsCoreSQLGrammar() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsANSI92FullSQL() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsOuterJoins() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsFullOuterJoins() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsLimitedOuterJoins() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public String getSchemaTerm() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public String getProcedureTerm() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public String getCatalogTerm() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean isCatalogAtStart() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public String getCatalogSeparator() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsCatalogsInPrivilegeDefinitions() 
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsPositionedDelete() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsPositionedUpdate() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsSelectForUpdate() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsStoredProcedures() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsSubqueriesInExists() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsSubqueriesInIns() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsUnion() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsUnionAll() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxBinaryLiteralLength() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxCharLiteralLength() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxColumnNameLength() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxColumnsInGroupBy() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxColumnsInIndex() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxColumnsInOrderBy() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxColumnsInSelect() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxColumnsInTable() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxConnections() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxCursorNameLength() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxIndexLength() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxSchemaNameLength() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxProcedureNameLength() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxCatalogNameLength() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxRowSize() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxStatementLength() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxStatements() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxTableNameLength() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxTablesInSelect() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getMaxUserNameLength() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public int getDefaultTransactionIsolation() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

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

        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsDataManipulationTransactionsOnly() 
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getProcedures(final String catalog, 
                                   final String schemaPatterns, 
                                   final String procedureNamePattern) 
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of getProcedures

    /**
     * {@inheritDoc}
     */
    public ResultSet getProcedureColumns(final String catalog, 
                                         final String schemaPatterns, 
                                         final String procedureNamePattern,
                                         final String columnNamePattern)
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getTables(final String catalog, 
                               final String schemaPatterns, 
                               final String tableNamePattern, 
                               final String[] types) throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getSchemas() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getCatalogs() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getTableTypes() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getColumns(final String catalog,
                                final String schemaPattern,
                                final String tableNamePattern,
                                final String columnNamePattern)
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getColumnPrivileges(final String catalog,
                                         final String schema,
                                         final String table,
                                         final String columnNamePattern)
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getTablePrivileges(final String catalog,
                                        final String schemaPattern,
                                        final String tableNamePattern) 
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getBestRowIdentifier(final String catalog,
                                          final String schema,
                                          final String table,
                                          final int scope,
                                          final boolean nullable)
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getVersionColumns(final String catalog,
                                       final String schema,
                                       final String table)
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getPrimaryKeys(final String catalog,
                                    final String schema,
                                    final String table)
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getImportedKeys(final String catalog,
                                     final String schema,
                                     final String table)
        throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getExportedKeys(final String catalog,
                                     final String schema,
                                     final String table)
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of

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

        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getTypeInfo() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getIndexInfo(final String catalog,
                                  final String schema,
                                  final String table,
                                  final boolean unique,
                                  final boolean approximate)
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsResultSetType(final int type) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsResultSetConcurrency(final int type, 
                                                final int concurrency) 
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean ownUpdatesAreVisible(final int type) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean ownDeletesAreVisible(final int type) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean ownInsertsAreVisible(final int type) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean othersUpdatesAreVisible(final int type) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean othersDeletesAreVisible(final int type) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean othersInsertsAreVisible(final int type) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean updatesAreDetected(final int type) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean deletesAreDetected(final int type) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean insertsAreDetected(final int type) throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public boolean supportsBatchUpdates() throws SQLException {
        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getUDTs(String catalog,
                             String schemaPattern,
                             String typeNamePattern,
                             int[] types)
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
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

        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getSuperTables(final String catalog,
                                    final String schemaPattern,
                                    final String tableNamePattern)
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of

    /**
     * {@inheritDoc}
     */
    public ResultSet getAttributes(final String catalog,
                                   final String schemaPattern,
                                   final String typeNamePattern,
                                   final String attributeNamePattern)
        throws SQLException {

        throw new RuntimeException("Not yet implemented");
    } // end of

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
