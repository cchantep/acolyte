package acolyte.jdbc;

import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import java.math.BigDecimal;

import java.sql.Timestamp;
import java.sql.Array;
import java.sql.Types;
import java.sql.Date;
import java.sql.Time;

/**
 * Acolyte defaults.
 *
 * @author Cedric Chantepie
 */
final class Defaults {
    
    /**
     * JDBC type mappings
     */
    public static final Map<Integer,String> jdbcTypeMappings;

    /**
     * JDBC type names
     */
    public static final Map<Integer,String> jdbcTypeNames;

    /**
     * JDBC type signs
     */
    public static final Map<Integer,Boolean> jdbcTypeSigns;

    /**
     * JDBC type precisions
     */
    public static final Map<Integer,Integer> jdbcTypePrecisions;

    /**
     * JDBC type scales
     */
    public static final Map<Integer,Integer> jdbcTypeScales;

    /**
     * JDBC type classes
     */
    public static final Map<String,Integer> jdbcTypeClasses;

    /**
     * JDBC type name classes
     */
    public static final Map<String,String> jdbcTypeNameClasses;

    static {
        // JDBC type mappings
        final LinkedHashMap<Integer,String> mappings = new LinkedHashMap<Integer,String>();

        mappings.put(Types.ARRAY, Array.class.getName());
        mappings.put(Types.BIGINT, Long.class.getName());
        mappings.put(Types.BIT, Boolean.class.getName());
        mappings.put(Types.BOOLEAN, Boolean.class.getName());
        mappings.put(Types.CHAR, Character.class.getName());
        mappings.put(Types.DATE, Date.class.getName());
        mappings.put(Types.DECIMAL, BigDecimal.class.getName());
        mappings.put(Types.DOUBLE, Double.class.getName());
        mappings.put(Types.FLOAT, Float.class.getName());
        mappings.put(Types.INTEGER, Integer.class.getName());
        mappings.put(Types.NUMERIC, BigDecimal.class.getName());
        mappings.put(Types.REAL, Float.class.getName());
        mappings.put(Types.SMALLINT, Short.class.getName());
        mappings.put(Types.TINYINT, Byte.class.getName());
        mappings.put(Types.TIME, Time.class.getName());
        mappings.put(Types.TIMESTAMP, Timestamp.class.getName());
        mappings.put(Types.VARCHAR, String.class.getName());
        mappings.put(Types.LONGVARCHAR, String.class.getName());        
        mappings.put(Types.BINARY, byte[].class.getName());
        mappings.put(Types.VARBINARY, byte[].class.getName());
        mappings.put(Types.LONGVARBINARY, byte[].class.getName());
        mappings.put(Types.BLOB, java.sql.Blob.class.getName());
        mappings.put(Types.OTHER, Object.class.getName());

        jdbcTypeMappings = Collections.unmodifiableMap(mappings);

        // JDBC type classes
        final HashMap<String,Integer> classes = new HashMap<String,Integer>();

        for (final Integer t : mappings.keySet()) {
            if (t == Types.BIT 
                || t == Types.REAL
                || t == Types.DECIMAL
                || t == Types.VARBINARY
                || t == Types.LONGVARBINARY
                || t == Types.LONGVARCHAR) continue; // Skip

            classes.put(mappings.get(t), t);
        } // end of for

        jdbcTypeClasses = Collections.unmodifiableMap(classes);

        // JDBC type names
        final HashMap<Integer,String> names = new HashMap<Integer,String>();

        names.put(Types.ARRAY, "ARRAY");
        names.put(Types.BIGINT, "BIGINT");
        names.put(Types.BIT, "BOOL");
        names.put(Types.BOOLEAN, "BOOL");
        names.put(Types.CHAR, "CHAR");
        names.put(Types.DATE, "DATE");
        names.put(Types.DECIMAL, "DECIMAL");
        names.put(Types.DOUBLE, "DOUBLE");
        names.put(Types.FLOAT, "FLOAT");
        names.put(Types.INTEGER, "INTEGER");
        names.put(Types.LONGVARCHAR, "TEXT");
        names.put(Types.NUMERIC, "NUMERIC");
        names.put(Types.REAL, "FLOAT");
        names.put(Types.SMALLINT, "SMALLINT");
        names.put(Types.TINYINT, "TINYINT");
        names.put(Types.TIME, "TIME");
        names.put(Types.TIMESTAMP, "TIMESTAMP");
        names.put(Types.VARCHAR, "VARCHAR");
        names.put(Types.BINARY, "BINARY");
        names.put(Types.VARBINARY, "VARBINARY");
        names.put(Types.LONGVARBINARY, "LONGVARBINARY");
        names.put(Types.BLOB, "BLOB");
        names.put(Types.OTHER, "OTHER");

        jdbcTypeNames = Collections.unmodifiableMap(names);

        // Class for JDBC type name
        final HashMap<String,String> nameClasses = new HashMap<String,String>();

        for (final Map.Entry<Integer,String> e : jdbcTypeNames.entrySet()) {
            nameClasses.put(e.getValue(), jdbcTypeMappings.get(e.getKey()));
        } // end of for

        jdbcTypeNameClasses = nameClasses;

        // JDBC type signs
        final HashMap<Integer,Boolean> signs = new HashMap<Integer,Boolean>();

        signs.put(Types.ARRAY, Boolean.FALSE);
        signs.put(Types.BIGINT, Boolean.TRUE);
        signs.put(Types.BIT, Boolean.FALSE);
        signs.put(Types.BOOLEAN, Boolean.FALSE);
        signs.put(Types.CHAR, Boolean.FALSE);
        signs.put(Types.DATE, Boolean.FALSE);
        signs.put(Types.DECIMAL, Boolean.TRUE);
        signs.put(Types.DOUBLE, Boolean.TRUE);
        signs.put(Types.FLOAT, Boolean.TRUE);
        signs.put(Types.INTEGER, Boolean.TRUE);
        signs.put(Types.LONGVARCHAR, Boolean.FALSE);
        signs.put(Types.NUMERIC, Boolean.TRUE);
        signs.put(Types.REAL, Boolean.TRUE);
        signs.put(Types.SMALLINT, Boolean.TRUE);
        signs.put(Types.TINYINT, Boolean.TRUE);
        signs.put(Types.TIME, Boolean.FALSE);
        signs.put(Types.TIMESTAMP, Boolean.FALSE);
        signs.put(Types.VARCHAR, Boolean.FALSE);
        signs.put(Types.BINARY, Boolean.FALSE);
        signs.put(Types.VARBINARY, Boolean.FALSE);
        signs.put(Types.LONGVARBINARY, Boolean.FALSE);
        signs.put(Types.BLOB, Boolean.FALSE);
        signs.put(Types.OTHER, Boolean.FALSE);

        jdbcTypeSigns = Collections.unmodifiableMap(signs);

        // JDBC type precisions
        final HashMap<Integer,Integer> precisions = 
            new HashMap<Integer,Integer>();

        precisions.put(Types.ARRAY, 0);
        precisions.put(Types.BIGINT, 64);
        precisions.put(Types.BIT, 1);
        precisions.put(Types.BOOLEAN, 1);
        precisions.put(Types.CHAR, 16);
        precisions.put(Types.DATE, 0);
        precisions.put(Types.DECIMAL, 0);
        precisions.put(Types.DOUBLE, 64);
        precisions.put(Types.FLOAT, 32);
        precisions.put(Types.INTEGER, 32);
        precisions.put(Types.LONGVARCHAR, 0);
        precisions.put(Types.NUMERIC, 0);
        precisions.put(Types.REAL, 32);
        precisions.put(Types.SMALLINT, 16);
        precisions.put(Types.TINYINT, 16);
        precisions.put(Types.TIME, 0);
        precisions.put(Types.TIMESTAMP, 0);
        precisions.put(Types.VARCHAR, 0);
        precisions.put(Types.BINARY, 0);
        precisions.put(Types.VARBINARY, 0);
        precisions.put(Types.LONGVARBINARY, 0);
        precisions.put(Types.BLOB, 0);
        precisions.put(Types.OTHER, 0);

        jdbcTypePrecisions = Collections.unmodifiableMap(precisions);        

        // JDBC type scales
        final HashMap<Integer,Integer> scales = new HashMap<Integer,Integer>();

        scales.put(Types.ARRAY, 0);
        scales.put(Types.BIGINT, 0);
        scales.put(Types.BIT, 0);
        scales.put(Types.BOOLEAN, 0);
        scales.put(Types.CHAR, 0);
        scales.put(Types.DATE, 0);
        scales.put(Types.DECIMAL, 2);
        scales.put(Types.DOUBLE, 2);
        scales.put(Types.FLOAT, 2);
        scales.put(Types.INTEGER, 0);
        scales.put(Types.LONGVARCHAR, 0);
        scales.put(Types.NUMERIC, 2);
        scales.put(Types.REAL, 2);
        scales.put(Types.SMALLINT, 0);
        scales.put(Types.TINYINT, 0);
        scales.put(Types.TIME, 0);
        scales.put(Types.TIMESTAMP, 0);
        scales.put(Types.VARCHAR, 0);
        scales.put(Types.BINARY, 0);
        scales.put(Types.VARBINARY, 0);
        scales.put(Types.LONGVARBINARY, 0);
        scales.put(Types.BLOB, 0);
        scales.put(Types.OTHER, 0);

        jdbcTypeScales = Collections.unmodifiableMap(scales);
    } // end of <cinit>
} // end of class Defaults
