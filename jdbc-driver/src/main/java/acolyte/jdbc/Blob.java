package acolyte.jdbc;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.InputStream;

import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialBlob;

/**
 * Binary large object.
 *
 * @author Cedric Chantepie
 */
final class Blob implements java.sql.Blob {
    // --- Shared ---

    /**
     * Empty data
     */
    private static final byte[] NO_BYTE = new byte[0];

    /**
     * Empty stream
     */
    private static final ByteArrayInputStream NO_DATA =
        new ByteArrayInputStream(NO_BYTE);

    // --- Properties ---

    /**
     * Underlying blob
     */
    private java.sql.Blob underlying = null;

    // --- Constructors ---

    /**
     * No-arg constructor.
     */
    private Blob() throws SQLException { }

    /**
     * Bulk constructor.
     */
    public Blob(final byte[] bytes) throws SQLException {
        this.underlying = new SerialBlob(bytes);
    } // end of <init>

    /**
     * Returns nil BLOB.
     */
    public static Blob Nil() throws SQLException { return new Blob(); }

    // --- Blob implementation ---

    /**
     * {@inheritDoc}
     */
    public void free() throws SQLException {
        synchronized (this) {
            if (this.underlying == null) return;

            // ---

            this.underlying.free();
        } // end of sync
    } // end of free

    /**
     * {@inheritDoc}
     */
    public InputStream getBinaryStream() throws SQLException {
        synchronized (this) {
            if (this.underlying == null) return NO_DATA;

            // ---

            return this.underlying.getBinaryStream();
        } // end of sync
    } // end of getBinaryStream

    /**
     * {@inheritDoc}
     */
    public InputStream getBinaryStream(final long pos, final long length) 
        throws SQLException {

        synchronized (this) {
            if (this.underlying == null) {
                if (pos > 1) {
                    throw new SQLException("Invalid position: " + pos);
                } // end of if

                return NO_DATA;
            } // end of if

            // ---

            return  this.underlying.getBinaryStream(pos, length);
        } // end of sync
    } // end of getBinaryStream

    /**
     * {@inheritDoc}
     */
    public byte[] getBytes(final long pos, final int length)
        throws SQLException {

        synchronized (this) {
            if (this.underlying == null) {
                if (pos > 1) {
                    throw new SQLException("Invalid position: " + pos);
                } // end of if

                return NO_BYTE;
            } // end of if

            // ---

            return this.underlying.getBytes(pos, length);
        } // end of sync
    } // end of getBytes

    /**
     * {@inheritDoc}
     */
    public long length() throws SQLException {
        synchronized (this) {
            if (this.underlying == null) {
                return 0L;
            } // end of if

            return this.underlying.length();
        } // end of sync
    } // end of length

    /**
     * {@inheritDoc}
     */
    public long position(final java.sql.Blob pattern, final long start)
        throws SQLException {

        synchronized (this) {
            if (this.underlying == null) {
                if (start > 1) {
                    throw new SQLException("Invalid offset: " + start);
                } // end of if

                return -1L;
            } // end of if

            return this.underlying.position(pattern, start);
        } // end of sync
    } // end of position

    /**
     * {@inheritDoc}
     */
    public long position(final byte[] pattern, final long start)
        throws SQLException {

        synchronized (this) {
            if (this.underlying == null) {
                if (start > 1) {
                    throw new SQLException("Invalid offset: " + start);
                } // end of if

                return -1L;
            } // end of if

            return this.underlying.position(pattern, start);
        } // end of sync
    } // end of position

    /**
     * {@inheritDoc}
     * @throw SQLFeatureNotSupportedException if this BLOB is empty
     */
    public OutputStream setBinaryStream(final long pos) throws SQLException {
        synchronized (this) {
            if (this.underlying == null) {
                if (pos > 1) {
                    throw new SQLException("Invalid position: " + pos);
                } // end of if

                throw new SQLFeatureNotSupportedException("Cannot write to empty BLOB");
            } // end of if

            // ---

            return this.underlying.setBinaryStream(pos);
        } // end of sync
    } // end of setBinaryStream

    /**
     * {@inheritDoc}
     * @see #setBytes(long, bytes[], offset, int)
     */
    public int setBytes(final long pos, byte[] bytes) throws SQLException {
        if (bytes == null) {
            throw new IllegalArgumentException("No byte to be set");
        } // end of if

        return setBytes(pos, bytes, 0, bytes.length);
    } // end of setBytes

    /**
     * {@inheritDoc}
     */
    public int setBytes(final long pos, byte[] bytes,
                        final int offset, final int len) throws SQLException {

        synchronized (this) {
            if (this.underlying == null) {
                if (pos > 1) {
                    throw new SQLException("Invalid position: " + pos);
                } // end of if

                if (len < 0) {
                    throw new IllegalArgumentException("Invalid bytes length: " 
                                                       + len);

                } // end of if

                // ---

                final byte[] copy = new byte[len];

                try {
                    System.arraycopy(bytes, offset, copy, 0, len);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Fails to prepare binary data", e);
                } // end of catch

                // ---

                this.underlying = new SerialBlob(copy);

                return len;
            } // end of if

            // ---

            return this.underlying.setBytes(pos, bytes, offset, len);
        } // end of sync
    } // end of setBytes

    /**
     * {@inheritDoc}
     */
    public void truncate(final long len) throws SQLException {
        synchronized (this) {
            if (this.underlying == null) {
                if (len < 0) {
                    throw new SQLException("Invalid length: " + len);
                } // end of if

                return;
            } // end of if

            // ---

            this.underlying.truncate(len);
        } // end of truncate
    } // end of truncate

    // --- Object support ---

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Blob)) {
            return false;
        } // end of if

        // ---

        final Blob other = (Blob) o;

        return ((this.underlying == null && other.underlying == null) ||
                (this.underlying != null && 
                 this.underlying.equals(other.underlying)));

    } // end of equals

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return (this.underlying == null) ? 3 : this.underlying.hashCode();
    } // end of hashCode
} // end of class Blob
