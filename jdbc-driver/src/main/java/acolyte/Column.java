package acolyte;

/**
 * Column meta data.
 *
 * @author Cedric Chantepie
 */
public final class Column<T> {
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
} // end of class Column
