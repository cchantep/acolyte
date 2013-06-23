package acolyte;

import java.util.Iterator;

/**
 * Row reader.
 *
 * @author Cedric Chantepie
 */
public interface RowReader {

    /**
     * Reads values of next row (as text/String).
     */
    public Iterator<String> readValues();

} // end of interface RowReader
