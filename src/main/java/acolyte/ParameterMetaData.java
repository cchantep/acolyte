package acolyte;

import java.util.List;

import java.sql.SQLException;

import static acolyte.Defaults.*;

/**
 * Acolyte parameter meta-data.
 *
 * @author Cedric Chantepie
 */
public final class ParameterMetaData implements java.sql.ParameterMetaData {
    // --- Properties ---

    /**
     * Definitions
     */
    private final List<Parameter> parameters;

    // --- Constructors ---

    /**
     * Constructor.
     */
    public ParameterMetaData(final List<Parameter> parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("Missing definition");
        } // end of if

        this.parameters = parameters;
    } // end of <init>

    // ---
    
    /**
     * {@inheritDoc}
     */
    public int getParameterCount() throws SQLException {
        return this.parameters.size();
    } // end of getParameterCount
 
    /**
     * {@inheritDoc}
     */
    public int isNullable(final int param) throws SQLException {
        try {
            return this.parameters.get(param-1).nullable;
        } catch (IndexOutOfBoundsException out) {
            throw new SQLException("Parameter out of bounds: " + param);
        } // end of catch
    } // end of isNullable
 
    /**
     * {@inheritDoc}
     */
    public boolean isSigned(final int param) throws SQLException {
        try {
            return this.parameters.get(param-1).signed;
        } catch (IndexOutOfBoundsException out) {
            throw new SQLException("Parameter out of bounds: " + param);
        } // end of catch
    } // end of isSigned
 
    /**
     * {@inheritDoc}
     */
    public int getPrecision(final int param) throws SQLException {
        try {
            return this.parameters.get(param-1).precision;
        } catch (IndexOutOfBoundsException out) {
            throw new SQLException("Parameter out of bounds: " + param);
        } // end of catch
    } // end of getPrecision
 
    /**
     * {@inheritDoc}
     */
    public int getScale(final int param) throws SQLException {
        try {
            return this.parameters.get(param-1).scale;
        } catch (IndexOutOfBoundsException out) {
            throw new SQLException("Parameter out of bounds: " + param);
        } // end of catch
    } // end of getScale
 
    /**
     * {@inheritDoc}
     */
    public int getParameterType(final int param) throws SQLException {
        try {
            return this.parameters.get(param-1).sqlType;
        } catch (IndexOutOfBoundsException out) {
            throw new SQLException("Parameter out of bounds: " + param);
        } // end of catch
    } // end of getParameterType
 
    /**
     * {@inheritDoc}
     */
    public String getParameterTypeName(final int param) throws SQLException {
        try {
            return this.parameters.get(param-1).sqlTypeName;
        } catch (IndexOutOfBoundsException out) {
            throw new SQLException("Parameter out of bounds: " + param);
        } // end of catch
    } // end of getParameterTypeName
 
    /**
     * {@inheritDoc}
     */
    public String getParameterClassName(final int param) throws SQLException {
        try {
            return this.parameters.get(param-1).className;
        } catch (IndexOutOfBoundsException out) {
            throw new SQLException("Parameter out of bounds: " + param);
        } // end of catch
    } // end of getParameterClassName
 
    /**
     * {@inheritDoc}
     */
    public int getParameterMode(final int param) throws SQLException {
        try {
            return this.parameters.get(param-1).mode;
        } catch (IndexOutOfBoundsException out) {
            throw new SQLException("Parameter out of bounds: " + param);
        } // end of catch
    } // end of getParameterMode
    
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

    // --- Factory methods ---

    /**
     * Null constructor.
     */
    public static Parameter Null(final int sqlType) {
        return new Parameter(jdbcTypeMappings.get(sqlType),
                             parameterModeIn,
                             sqlType,
                             jdbcTypeNames.get(sqlType),
                             jdbcTypePrecisions.get(sqlType),
                             jdbcTypeScales.get(sqlType),
                             parameterNullableUnknown,
                             jdbcTypeSigns.get(sqlType));

    } // end of Null

    // --- Inner classes ---

    /**
     * Single parameter definition.
     */
    public static final class Parameter {
        public final String className;
        public final int mode;
        public final int sqlType;
        public final String sqlTypeName;
        public final int precision;
        public final int scale;
        public final int nullable;
        public final boolean signed;

        // --- Constructors ---

        /**
         * Bulk constructor
         */
        public Parameter(final String className,
                         final int mode,
                         final int sqlType,
                         final String sqlTypeName,
                         final int precision,
                         final int scale,
                         final int nullable,
                         final boolean signed) {

            if (className == null) {
                throw new IllegalArgumentException("Missing class name");
            } // end of if

            if (sqlTypeName == null) {
                throw new IllegalArgumentException("Missing SQL type name");
            } // end of if

            // ---

            this.className = className;
            this.mode = mode;
            this.sqlType = sqlType;
            this.sqlTypeName = sqlTypeName;
            this.precision = precision;
            this.scale = scale;
            this.nullable = nullable;
            this.signed = signed;
        } // end of <init>
    } // end of class Parameter
} // end of class ParameterMetaData
