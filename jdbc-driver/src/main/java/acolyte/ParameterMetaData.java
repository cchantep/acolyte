package acolyte;

import java.util.Locale;
import java.util.List;

import java.math.BigDecimal;

import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

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
    public final List<ParameterDef> parameters;

    // --- Constructors ---

    /**
     * Constructor.
     */
    public ParameterMetaData(final List<ParameterDef> parameters) {
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
        } catch (NullPointerException e) {
            throw new SQLException("Parameter is not set: " + param);
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
        } catch (NullPointerException e) {
            throw new SQLException("Parameter is not set: " + param);
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
        } catch (NullPointerException e) {
            throw new SQLException("Parameter is not set: " + param);
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
        } catch (NullPointerException e) {
            throw new SQLException("Parameter is not set: " + param);
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
        } catch (NullPointerException e) {
            throw new SQLException("Parameter is not set: " + param);
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
        } catch (NullPointerException e) {
            throw new SQLException("Parameter is not set: " + param);
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
        } catch (NullPointerException e) {
            throw new SQLException("Parameter is not set: " + param);
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
        } catch (NullPointerException e) {
            throw new SQLException("Parameter is not set: " + param);
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
     * Default parameter.
     */
    public static ParameterDef Default(final int sqlType) {
        return new ParameterDef(jdbcTypeMappings.get(sqlType),
                                parameterModeIn,
                                sqlType,
                                jdbcTypeNames.get(sqlType),
                                jdbcTypePrecisions.get(sqlType),
                                jdbcTypeScales.get(sqlType),
                                parameterNullableUnknown,
                                jdbcTypeSigns.get(sqlType));
    } // end of Default

    /**
     * Decimal parameter.
     */
    public static ParameterDef Scaled(final int sqlType, final int scale) {
        return new ParameterDef(jdbcTypeMappings.get(sqlType),
                                parameterModeIn,
                                sqlType,
                                jdbcTypeNames.get(sqlType),
                                jdbcTypePrecisions.get(sqlType),
                                scale,
                                parameterNullableUnknown,
                                jdbcTypeSigns.get(sqlType));

    } // end of Decimal

    /**
     * Null constructor.
     */
    public static ParameterDef Null(final int sqlType) {
        return Default(sqlType);
    } // end of Null

    /**
     * Boolean constructor.
     */
    public static ParameterDef Bool() {
        return Default(Types.BOOLEAN);
    } // end of Bool

    /**
     * Byte constructor.
     */
    public static ParameterDef Byte() {
        return Default(Types.TINYINT);
    } // end of Byte

    /**
     * Short constructor.
     */
    public static ParameterDef Short() {
        return Default(Types.SMALLINT);
    } // end of Short

    /**
     * Integer constructor.
     */
    public static ParameterDef Int() {
        return Default(Types.INTEGER);
    } // end of Int

    /**
     * Long constructor.
     */
    public static ParameterDef Long() {
        return Default(Types.BIGINT);
    } // end of Long

    /**
     * Float constructor.
     */
    public static ParameterDef Float(final float f) {
        final BigDecimal bd = new BigDecimal(Float.toString(f));

        return Scaled(Types.FLOAT, bd.scale());
    } // end of Float

    /**
     * Float constructor (as REAL).
     */
    public static ParameterDef Real(final float f) {
        final BigDecimal bd = new BigDecimal(Float.toString(f));

        return Scaled(Types.REAL, bd.scale());
    } // end of Real

    /**
     * Double constructor.
     */
    public static ParameterDef Double(final double d) {
        final BigDecimal bd = new BigDecimal(String.format(Locale.US, "%f", d)).
            stripTrailingZeros();

        return Scaled(Types.DOUBLE, bd.scale());
    } // end of Double

    /**
     * BigDecimal constructor.
     */
    public static ParameterDef Numeric(final BigDecimal bd) {
        return Scaled(Types.NUMERIC, bd.scale());
    } // end of BigDecimal

    /**
     * BigDecimal constructor (as DECIMAL).
     */
    public static ParameterDef Decimal(final BigDecimal bd) {
        return Scaled(Types.DECIMAL, bd.scale());
    } // end of Decimal

    /**
     * String constructor.
     */
    public static ParameterDef Str() {
        return Default(Types.VARCHAR);
    } // end of Str

    /**
     * Date constructor.
     */
    public static ParameterDef Date() {
        return Default(Types.DATE);
    } // end of Date

    /**
     * Time constructor.
     */
    public static ParameterDef Time() {
        return Default(Types.TIME);
    } // end of Time

    /**
     * Timestamp constructor.
     */
    public static ParameterDef Timestamp() {
        return Default(Types.TIMESTAMP);
    } // end of Timestamp

    // --- Inner classes ---

    /**
     * Single parameter definition.
     */
    public static final class ParameterDef {
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
        public ParameterDef(final String className,
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

        // ---

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return String.format("ParameterDef(class = %s, mode = %s, sqlType = %s(%d), precision = %d, scale = %d, nullable = %s, signed = %s)", this.className, this.mode, this.sqlTypeName, this.sqlType, this.precision, this.scale, this.nullable, this.signed);

        } // end of toString

        /**
         * {@inheritDoc}
         */
        public boolean equals(Object o) {
            if (o == null || !(o instanceof ParameterDef)) {
                return false;
            } // end of if

            final ParameterDef other = (ParameterDef) o;

            return new EqualsBuilder().
                append(this.className, other.className).
                append(this.mode, other.mode).
                append(this.sqlType, other.sqlType).
                append(this.sqlTypeName, other.sqlTypeName).
                append(this.precision, other.precision).
                append(this.scale, other.scale).
                append(this.nullable, other.nullable).
                append(this.signed, other.signed).
                isEquals();

        } // end of equals

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return new HashCodeBuilder(11, 1).
                append(this.className).
                append(this.mode).
                append(this.sqlType).
                append(this.sqlTypeName).
                append(this.precision).
                append(this.scale).
                append(this.nullable).
                append(this.signed).
                toHashCode();

        } // end of hashCode
    } // end of class Parameter
} // end of class ParameterMetaData
