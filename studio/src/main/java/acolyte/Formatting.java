package acolyte;

/**
 * Formatting options.
 *
 * @author Cedric Chantepie
 */
enum Formatting {
    // --- Instances ---

    /**
     * Java formatting
     */
    Java("Java",
         ".append(" /* rowStart */,
         ")\r\n" /* rowEnd */,
         ", " /* valueSeparator */,
         "new java.math.BigDecimal(\"%s\")" /* someBigDecimal */,
         "(java.math.BigDecimal)null" /* noneBigDecimal */,
         "%b" /* someBoolean */,
         "(Boolean)null" /* noneBoolean */,
         "%d.toByte" /* someByte */,
         "(Byte)null" /* noneByte */,
         "%d.toShort" /* someShort */,
         "(Short)null" /* noneShort */,
         "new java.sql.Date(%dl)" /* someDate */,
         "(java.sql.Date)null" /* noneDate */,
         "%fd" /* someDouble */,
         "(Double)null" /* noneDouble */,
         "%f" /* someFloat */,
         "(Float)null" /* noneFloat */,
         "%d" /* someInt */,
         "(Integer)null" /* noneInt */,
         "%dl" /* someLong */,
         "(Long)null" /* noneLong */,
         "new java.sql.Time(%dl)" /* someTime */,
         "(java.sql.Time)null" /* noneTime */,
         "new java.sql.Timestamp(%dl)" /* someTimestamp */,
         "(java.sql.Timestamp)null" /* noneTimestamp */,
         "\"%s\"" /* someString */,
         "(String)null" /* noneString */),

    /**
     * Scala formatting
     */
    Scala("Scala",
          ".append(" /* rowStart */,
          ")\r\n" /* rowEnd */,
          ", " /* valueSeparator */,
          "new java.math.BigDecimal(\"%s\")" /* someBigDecimal */,
          "null.asInstanceOf[java.math.BigDecimal]" /* noneBigDecimal */,
          "%b" /* someBoolean */,
          "null.asInstanceOf[Boolean]" /* noneBoolean */,
          "%d.toByte" /* someByte */,
          "null.asInstanceOf[Byte]" /* noneByte */,
          "%d.toShort" /* someShort */,
          "null.asInstanceOf[Short]" /* noneShort */,
          "new java.sql.Date(%dl)" /* someDate */,
          "null.asInstance[java.sql.Date]" /* noneDate */,
          "%fd" /* someDouble */,
          "null.asInstanceOf[Double]" /* noneDouble */,
          "%f" /* someFloat */,
          "null.asInstanceOf[Float]" /* noneFloat */,
          "%d" /* someInt */,
          "null.asInstanceOf[Int]" /* noneInt */,
          "%dl" /* someLong */,
          "null.asInstanceOf[Long]" /* noneLong */,
          "new java.sql.Time(%dl)" /* someTime */,
          "null.asInstanceOf[java.sql.Time]" /* noneTime */,
          "new java.sql.Timestamp(%dl)" /* someTimestamp */,
          "null.asInstanceOf[java.sql.Timestamp]" /* noneTimestamp */,
          "\"%s\"" /* someString */,
          "null.asInstanceOf[String]" /* noneString */);

    // --- Properties ---

    /**
     * Format name
     */
    public final String format;

    /**
     * Row start
     */
    public final String rowStart;

    /**
     * Row end
     */
    public final String rowEnd;

    /**
     * Value separator
     */
    public final String valueSeparator;

    /**
     * Not-null big decimal pattern
     */
    public final String someBigDecimal;

    /**
     * Null big decimal pattern
     */
    public final String noneBigDecimal;

    /**
     * Not-null boolean pattern
     */
    public final String someBoolean;

    /**
     * Null boolean pattern
     */
    public final String noneBoolean;

    /**
     * Not-null byte pattern
     */
    public final String someByte;

    /**
     * Null byte pattern
     */
    public final String noneByte;

    /**
     * Not-null short pattern
     */
    public final String someShort;

    /**
     * Null short pattern
     */
    public final String noneShort;

    /**
     * Not-null date pattern
     */
    public final String someDate;

    /**
     * Null date pattern
     */
    public final String noneDate;

    /**
     * Not-null double pattern
     */
    public final String someDouble;

    /**
     * Null double pattern
     */
    public final String noneDouble;

    /**
     * Not-null float pattern
     */
    public final String someFloat;

    /**
     * Null float pattern
     */
    public final String noneFloat;

    /**
     * Not-null int pattern
     */
    public final String someInt;

    /**
     * Null int pattern
     */
    public final String noneInt;

    /**
     * Not-null long pattern
     */
    public final String someLong;

    /**
     * Null long pattern
     */
    public final String noneLong;

    /**
     * Not-null time pattern
     */
    public final String someTime;

    /**
     * Null time pattern
     */
    public final String noneTime;

    /**
     * Not-null timestamp pattern
     */
    public final String someTimestamp;

    /**
     * Null timestamp pattern
     */
    public final String noneTimestamp;

    /**
     * Not-null string pattern
     */
    public final String someString;

    /**
     * Null string pattern
     */
    public final String noneString;

    // --- Constructors ---

    /**
     * Returns formatting matching |format|.
     * @throws IllegalArgumentException if format is invalid or unsupported
     */
    public static Formatting forName(final String format) {
        if ("java".equals(format)) {
            return Java;
        } else if ("scala".equals(format)) {
            return Scala;
        } else {
            throw new IllegalArgumentException("Unsupported format: " + format);
        } // end of else
    } // end of forName

    /**
     * Bulk constructor.
     *
     * @param format Format name
     */
    private Formatting(final String format,
                       final String rowStart,
                       final String rowEnd,
                       final String valueSeparator,
                       final String someBigDecimal,
                       final String noneBigDecimal,
                       final String someBoolean,
                       final String noneBoolean,
                       final String someByte,
                       final String noneByte,
                       final String someShort,
                       final String noneShort,
                       final String someDate,
                       final String noneDate,
                       final String someDouble,
                       final String noneDouble,
                       final String someFloat,
                       final String noneFloat,
                       final String someInt,
                       final String noneInt,
                       final String someLong,
                       final String noneLong,
                       final String someTime,
                       final String noneTime,
                       final String someTimestamp,
                       final String noneTimestamp,
                       final String someString,
                       final String noneString) {

        this.format = format;
        this.rowStart = rowStart;
        this.rowEnd = rowEnd;
        this.valueSeparator = valueSeparator;
        this.someBigDecimal = someBigDecimal;
        this.noneBigDecimal = noneBigDecimal;
        this.someBoolean = someBoolean;
        this.noneBoolean = noneBoolean;
        this.someByte = someByte;
        this.noneByte = noneByte;
        this.someShort = someShort;
        this.noneShort = noneShort;
        this.someDate = someDate;
        this.noneDate = noneDate;
        this.someDouble = someDouble;
        this.noneDouble = noneDouble;
        this.someFloat = someFloat;
        this.noneFloat = noneFloat;
        this.someInt = someInt;
        this.noneInt = noneInt;
        this.someLong = someLong;
        this.noneLong = noneLong;
        this.someTime = someTime;
        this.noneTime = noneTime;
        this.someTimestamp = someTimestamp;
        this.noneTimestamp = noneTimestamp;
        this.someString = someString;
        this.noneString = noneString;
    } // end of <init>

    // --- Object support ---

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return String.format("Formatting(%s)", this.format);
    } // end of toString
} // end of enum Formatting
