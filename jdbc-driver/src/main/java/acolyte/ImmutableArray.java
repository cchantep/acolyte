package acolyte;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Array;

/**
 * Immu Array implementation.
 *
 * @author Cedric Chantepie
 */
final class ImmutableArray<T> { //implements Array {
    // --- Properties ---

    /**
     * Base (element) class
     */
    protected final Class<T> baseClass;

    /**
     * Base JDBC type (identifier)
     */
    protected final int baseType;

    /**
     * Base JDBC type name
     */
    protected final String baseTypeName;

    /**
     * Array elements
     */
    protected final List<T> elements;

    // --- Constructors ---

    /**
     * Constructor
     */
    private ImmutableArray(final Class<T> baseClass,
                           final List<T> elements) {

        if (baseClass == null) {
            throw new IllegalArgumentException("No base class");
        } // end of

        Integer jdbcType = Defaults.jdbcTypeClasses.get(baseClass.getName());

        if (jdbcType == null) {
            throw new IllegalArgumentException("Unsupported base class");
        } // end of if

        // ---

        this.baseClass = baseClass;
        this.baseType = jdbcType;
        this.baseTypeName = Defaults.jdbcTypeNames.get(jdbcType);
        this.elements = elements;
    } // end of <init>

    /**
     * Returns empty array for given base class.
     */
    protected static <A> ImmutableArray<A> getInstance(final Class<A> baseClass) { return new ImmutableArray<A>(baseClass, Collections.unmodifiableList(Collections.<A>emptyList())); }

    /**
     * Returns array with copy of given |elements|.
     */
    protected static <A> ImmutableArray<A> getInstance(final Class<A> baseClass, final A[] elements) { 
        if (elements == null) {
            throw new IllegalArgumentException("Invalid element array");
        } // end of if

        return new ImmutableArray<A>(baseClass, Arrays.<A>asList(elements)); 
    } // end of getInstance

    /**
     * Returns array with copy of given |elements|.
     */
    protected static <A> ImmutableArray<A> getInstance(final Class<A> baseClass, final List<A> elements) {
        if (elements == null) {
            throw new IllegalArgumentException("Invalid element list");
        } // end of if

        return new ImmutableArray<A>(baseClass, 
                                     Collections.unmodifiableList(elements));
    } // end of getInstance

    // ---

    /**
     * {@inheritDoc}
     */
    public int getBaseType() throws SQLException {
        return this.baseType;
    } // end of getBaseType

    /**
     * {@inheritDoc}
     */
    public String getBaseTypeName() throws SQLException { 
        return this.baseTypeName;
    } // end of getBaseTypeName

    /**
     * {@inheritDoc}
     */
    public Object getArray() throws SQLException {
        return this.elements.toArray();
    } // end of getArray

    /**
     * @throws SQLFeatureNotSupportedException as array convertion 
     * is not supported
     */
    public Object getArray(Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of getArray


    //public Object getArray(long, int) throws SQLException;

    /**
     * @throws SQLFeatureNotSupportedException as array convertion 
     * is not supported
     */
    public Object getArray(long index, 
                           int count, 
                           Map<String, Class<?>> map) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of getArray

    /**
     * {@inheritDoc}
     */
    public ResultSet getResultSet() throws SQLException {
        RowList1<T,?> rows = RowLists.rowList1(this.baseClass);

        for (final T elmt : this.elements) rows = rows.append(elmt);

        return rows.resultSet();
    } // end of getResultSet

    /**
     * @throws SQLFeatureNotSupportedException as array convertion 
     * is not supported
     */
    public ResultSet getResultSet(Map<String, Class<?>> map) 
        throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of getResultSet

    // public ResultSet getResultSet(long, int) throws SQLException;

    /**
     * @throws SQLFeatureNotSupportedException as array convertion 
     * is not supported
     */
    public ResultSet getResultSet(long index, 
                                  int count, 
                                  Map<String, Class<?>> map) 
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of getResultSet

    /**
     * Does nothing, as immutable.
     */
    public void free() throws SQLException {}

    // ---

    /**
     * Returns sub-list, or null if |index| is not valid.
     */
    private List<T> subList(final long index, int count) {
        return null;
    } // end of subList
} // end of class ImmutableArray
