package acolyte.jdbc;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Column meta data.
 *
 * @author Cedric Chantepie
 */
public final class Column<T> {
    // --- Properties ---

    /**
     * Column class
     */
    public final Class<T> columnClass;

    /**
     * Column name/label
     */
    public final String name;

    /**
     * Column is nullable? (default: false)
     */
    public final boolean nullable;

    // --- Constructors ---

    /**
     * Bulk constructor.
     */
    Column(final Class<T> columnClass, 
           final String name,
           final boolean nullable) {

        if (columnClass == null) {
            throw new IllegalArgumentException("No column class");
        } // end of if

        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Invalid column name: " + 
                                               name);

        } // end of if

        this.columnClass = columnClass;
        this.name = name;
        this.nullable = nullable;
    } // end of <init>

    /**
     * Creates a not nullable column.
     *
     * @param columnClass the class of the column value
     * @param name the colunm name
     */
    Column(final Class<T> columnClass, final String name) {
        this(columnClass, name, false);
    } // end of <init>

    // --- 

    /**
     * Returns similar metadata, but with specified |nullable| flag.
     *
     * @param nullable Whether new metadata is nullable
     */
    public Column withNullable(final boolean nullable) {
        return new Column(this.columnClass, this.name, nullable);
    } // end of withNullable

    // --- Object support ---

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder(9, 3).
            append(this.columnClass).append(this.name).append(this.nullable).
            toHashCode();

    } // end of hashCode

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object o) {
        if (o == null || !(o instanceof Column)) {
            return false;
        } // end of if

        final Column other = (Column) o;

        return new EqualsBuilder().
            append(this.columnClass, other.columnClass).
            append(this.name, other.name).
            append(this.nullable, other.nullable).isEquals();

    } // end of equals 

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return String.format("Column(%s, %s, %s)", 
                             this.columnClass, this.name, this.nullable);

    } // end of toString
} // end of class Column
