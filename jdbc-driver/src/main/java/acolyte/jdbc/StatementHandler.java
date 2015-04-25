package acolyte.jdbc;

import java.util.List;

import java.sql.SQLException;
import java.sql.ResultSet;

import org.apache.commons.lang3.tuple.Pair;

import acolyte.jdbc.ParameterMetaData.ParameterDef;

/**
 * Statement handler: allow to process statement by 'hand' and return.
 *
 * @author Cedric Chantepie
 */
public interface StatementHandler {
    /**
     * When given |sql| query is executed against Acolyte connection ...
     *
     * @param sql SQL query (with '?' for prepared/callable statement)
     * @param parameters Parameters (or empty map if none)
     * @return Query result set
     * @throws SQLException if fails to handle the specified query
     */
    public QueryResult whenSQLQuery(String sql, List<Parameter> parameters) 
        throws SQLException;

    /**
     * When given |sql| update is executed against Acolyte connection ...
     *
     * @param sql SQL query (with '?' for prepared/callable statement)
     * @param parameters Parameters (or empty map if none)
     * @return Update result
     * @throws SQLException if fails to handle the specified update
     */
    public UpdateResult whenSQLUpdate(String sql, List<Parameter> parameters) 
        throws SQLException;

    /**
     * If statement is neither a PreparedStatement nor a CallbableStatement,
     * determines whether given |sql| is a query.
     *
     * If returns true, whenSQLQuery will be called.
     *
     * @param sql the SQL statement to be checked
     * @return true if the given SQL statement is a query, or false
     */
    public boolean isQuery(String sql);

    // --- Inner classes ---

    /**
     * Meaningful, user-friendly and immutable type alias 
     * for ugly Pair&lt;ParameterDef,Object&gt;.
     */
    public final class Parameter extends Pair<ParameterDef,Object> {
        public final ParameterDef left;
        public final Object right;
        
        // --- Constructors ---

        /**
         * Copy constructor.
         *
         * @param left the definition of the parameter
         * @param right the parameter value
         */
        private Parameter(final ParameterDef left, final Object right) {
            this.left = left;
            this.right = right;
        } // end of <init>

        // ---

        /**
         * Returns parameter made of |left| and |right| datas.
         *
         * @param left the definition of the parameter
         * @param right the parameter value
         * @return New parameter
         */
        public static Parameter of(final ParameterDef left, 
                                   final Object right) {

            return new Parameter(left, right);
        } // end of of

        /**
         * {@inheritDoc}
         */
        public ParameterDef getLeft() {
            return this.left;
        } // end of getLeft

        /**
         * {@inheritDoc}
         */
        public Object getRight() {
            return this.right;
        } // end of getRight

        /**
         * {@inheritDoc}
         */
        public Object getValue() {
            return this.right;
        } // end of getValue

        /**
         * @param value the parameter value
         * @throws UnsupportedOperationException
         */
        public Object setValue(final Object value) {
            throw new UnsupportedOperationException();
        } // end of setValue

        /**
         * Compares this parameter with an|other| one.
         *
         * @param other another parameter to compare this one with
         * @return &lt;0 if this parameter is before, 0 if equal, &gt;0 if after
         */
        public int compareTo(final Parameter other) {
            return super.compareTo(other);
        } // end of compareTo

        /**
         * {@inheritDoc}
         */
        public boolean equals(final Object o) {
            if (o == null || !(o instanceof Parameter)) {
                return false;
            } // end of if

            return super.equals(o);
        } // end of equals
        
        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return super.hashCode();
        } // end of hashCode

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return super.toString();
        } // end of toString

        /**
         * {@inheritDoc}
         */
        public String toString(final String format) {
            return super.toString(format);
        } // end of toString
    } // end of class Parameter
} // end of interface StatementHandler
