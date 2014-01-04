package acolyte;

import java.util.HashMap;

/**
 * Column types for mapping.
 *
 * @author Cedric Chantepie 
 */
public enum ColumnType {
    // --- Instances ---

    BigDecimal("bigdecimal"),
    Boolean("bool"),
    Byte("byte"),
    Short("short"), 
    Date("date"), 
    Double("double"), 
    Float("float"),
    Int("int"), 
    Long("long"), 
    Time("time"), 
    Timestamp("timestamp"), 
    String("string");

    private static final HashMap<String,ColumnType> types;

    static {
        final HashMap<String,ColumnType> map = new HashMap<String,ColumnType>();

        map.put("bigdecimal", BigDecimal);
        map.put("bool", Boolean);
        map.put("byte", Byte);
        map.put("short", Short);
        map.put("date", Date);
        map.put("double", Double);
        map.put("float", Float);
        map.put("int", Int);
        map.put("long", Long);
        map.put("time", Time);
        map.put("timestamp", Timestamp);
        map.put("string", String);

        types = map;
    } // end of <cinit>

    // --- Properties ---

    /**
     * Type name
     */
    public final String name;

    // --- Constructors ---

    /**
     * Bulk constructor.
     *
     * @param name Type name
     */
    private ColumnType(final String name) {
        this.name = name;
    } // end of <init>

    /**
     * Returns column type matching given |name|.
     *
     * @param name Type name
     */
    public static ColumnType typeFor(final String name) {
        return types.get(name);
    } // end of typeFor

    // ---

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return this.name;
    } // end of toString
} // end of class ColumnType
