package nars.util.data;

import com.google.common.primitives.Longs;
import com.google.common.primitives.UnsignedBytes;
import sun.misc.Unsafe;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;

/**
 *
 * @author https://github.com/squito/jutf8
 *
 * and
 *
 * Utility code to do optimized byte-array comparison.
 * This is borrowed and slightly modified from Guava's {@link UnsignedBytes}
 * class to be able to compare arrays that start at non-zero offsets.
 * from: https://svn.apache.org/repos/asf/cassandra/trunk/src/java/org/apache/cassandra/utils/FastByteComparisons.java
 *
 * http://svn.apache.org/viewvc/avro/trunk/lang/java/avro/src/main/java/org/apache/avro/other/Utf8.java?revision=1552418&view=co
 */
public class Utf8  implements CharSequence, Comparable<Utf8> {

    final byte[] bytes;
    final int start;
    final int end;
    int length = -1;
    int hash = 0;

    public static final Charset utf8Charset = Charset.forName("UTF-8");

    protected Utf8(byte[] bytes, int start, int end, int length) {
        this.bytes = bytes;
        this.start = start;
        this.end = end;
        this.length = length;
    }

    public Utf8(byte[] bytes, int start, int end) {
        this(bytes, 0, bytes.length, computeLength(bytes, start, end));
    }

    public Utf8(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    public Utf8(String s) {
        this(s.getBytes(utf8Charset));
    }

    public static final String fromUtf8(final byte[] bytes, final int length) {
        return new String(bytes, 0, length, utf8Charset);
    }
    public static final String fromUtf8(final byte[] bytes) {
        return new String(bytes, utf8Charset);
    }

    public static final byte[] toUtf8(final String str) {
        return str.getBytes(utf8Charset);
    }

    /**
     * Lexicographically compare two byte arrays.
     */
    final public static int compareTo(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
        return LexicographicalComparerHolder.BEST_COMPARER.compareTo(
                b1, s1, l1, b2, s2, l2);
    }

    final public static int compare(final byte[] a, final byte[] b) {
        if (a == b) return 0;
        return Utf8.compareTo(a, 0, a.length, b, 0, b.length);
    }

    private static Comparer<byte[]> lexicographicalComparerJavaImpl() {
        return LexicographicalComparerHolder.PureJavaComparer.INSTANCE;
    }

    @Override
    public int compareTo(Utf8 that) {
        int lDiff = that.bytes.length - bytes.length;
        if (lDiff != 0) return lDiff;
        for (int n = 0; n < bytes.length; n++) {
            int bDiff = that.bytes[n] - bytes[n];
            if (bDiff!=0) return bDiff;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Arrays.hashCode(bytes);
        }
        return hash;
    }

    @Override
    public int length() {
        if (length == -1) {
            length = computeLength(bytes, start, end);
        }
        return length;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        
        if (!(obj instanceof Utf8))
            return false;
       
        Utf8 u = (Utf8) obj;
        if (hashCode() != u.hashCode())
            return false;
        
        if (length != u.length)
            return false;
        
        return Arrays.equals(bytes, u.bytes);
    }

    public void commit() {
        length = -1;
        hash = 0;
    }

    /*@Override
     public char charAt(int index) {
     return (char) bytes[index];
     }*/
    @Override
    public char charAt(int index) {
        return toString().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    public static int computeLength(final byte[] bytes, final int start, final int end) {
        return utf8Charset.decode(ByteBuffer.wrap(bytes, start, end - start)).length();
    }

    @Override
    public String toString() {
        if (this.length == 0) {
            return "";
        }
        return fromUtf8(bytes, length);
    }

    private interface Comparer<T> {
        abstract public int compareTo(T buffer1, int offset1, int length1,
                                      T buffer2, int offset2, int length2);
    }

    /**
     * Provides a lexicographical comparer implementation; either a Java
     * implementation or a faster implementation based on {@link Unsafe}.
     *
     * <p>Uses reflection to gracefully fall back to the Java implementation if
     * {@code Unsafe} isn't available.
     */
    private static class LexicographicalComparerHolder {
        static final String UNSAFE_COMPARER_NAME =
                LexicographicalComparerHolder.class.getName() + "$UnsafeComparer";

        static final Comparer<byte[]> BEST_COMPARER = getBestComparer();
        /**
         * Returns the Unsafe-using Comparer, or falls back to the pure-Java
         * implementation if unable to do so.
         */
        static Comparer<byte[]> getBestComparer() {
            try {
                Class<?> theClass = Class.forName(UNSAFE_COMPARER_NAME);

                // yes, UnsafeComparer does implement Comparer<byte[]>
                @SuppressWarnings("unchecked")
                Comparer<byte[]> comparer =
                        (Comparer<byte[]>) theClass.getEnumConstants()[0];
                return comparer;
            } catch (Throwable t) { // ensure we really catch *everything*
                return lexicographicalComparerJavaImpl();
            }
        }

        private enum PureJavaComparer implements Comparer<byte[]> {
            INSTANCE;

            @Override
            public int compareTo(byte[] buffer1, int offset1, int length1,
                                 byte[] buffer2, int offset2, int length2) {
                // Short circuit equal case
                if (buffer1 == buffer2 &&
                        offset1 == offset2 &&
                        length1 == length2) {
                    return 0;
                }
                int end1 = offset1 + length1;
                int end2 = offset2 + length2;
                for (int i = offset1, j = offset2; i < end1 && j < end2; i++, j++) {
                    int a = (buffer1[i] & 0xff);
                    int b = (buffer2[j] & 0xff);
                    if (a != b) {
                        return a - b;
                    }
                }
                return length1 - length2;
            }
        }

        @SuppressWarnings("unused") // used via reflection
        private enum UnsafeComparer implements Comparer<byte[]> {
            INSTANCE;

            static final Unsafe theUnsafe;

            /** The offset to the first element in a byte array. */
            static final int BYTE_ARRAY_BASE_OFFSET;

            static {
                theUnsafe = (Unsafe) AccessController.doPrivileged(
                        new PrivilegedAction<Object>() {
                            @Override
                            public Object run() {
                                try {
                                    Field f = Unsafe.class.getDeclaredField("theUnsafe");
                                    f.setAccessible(true);
                                    return f.get(null);
                                } catch (NoSuchFieldException e) {
                                    // It doesn't matter what we throw;
                                    // it's swallowed in getBestComparer().
                                    throw new Error();
                                } catch (IllegalAccessException e) {
                                    throw new Error();
                                }
                            }
                        });

                BYTE_ARRAY_BASE_OFFSET = theUnsafe.arrayBaseOffset(byte[].class);

                // sanity check - this should never fail
                if (theUnsafe.arrayIndexScale(byte[].class) != 1) {
                    throw new AssertionError();
                }
            }

            static final boolean littleEndian =
                    ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN);

            /**
             * Returns true if x1 is less than x2, when both values are treated as
             * unsigned.
             */
            static boolean lessThanUnsigned(long x1, long x2) {
                return (x1 + Long.MIN_VALUE) < (x2 + Long.MIN_VALUE);
            }

            /**
             * Lexicographically compare two arrays.
             *
             * @param buffer1 left operand
             * @param buffer2 right operand
             * @param offset1 Where to start comparing in the left buffer
             * @param offset2 Where to start comparing in the right buffer
             * @param length1 How much to compare from the left buffer
             * @param length2 How much to compare from the right buffer
             * @return 0 if equal, < 0 if left is less than right, etc.
             */
            @Override
            public int compareTo(final byte[] buffer1, final int offset1, final int length1,
                                 final byte[] buffer2, final int offset2, final int length2) {
                // Short circuit equal case
                if (buffer1 == buffer2 &&
                        offset1 == offset2 &&
                        length1 == length2) {
                    return 0;
                }
                int minLength = length1 < length2 ? length1 : length2;
                int minWords = minLength / Longs.BYTES;
                int offset1Adj = offset1 + BYTE_ARRAY_BASE_OFFSET;
                int offset2Adj = offset2 + BYTE_ARRAY_BASE_OFFSET;

    /*
     * Compare 8 bytes at a time. Benchmarking shows comparing 8 bytes at a
     * time is no slower than comparing 4 bytes at a time even on 32-bit.
     * On the other hand, it is substantially faster on 64-bit.
     */
                final Unsafe tu = theUnsafe;
                final boolean e = littleEndian;
                for (int i = 0; i < minWords * Longs.BYTES; i += Longs.BYTES) {
                    final long li = (long)i;
                    long lw = tu.getLong(buffer1, offset1Adj + li);
                    long rw = tu.getLong(buffer2, offset2Adj + li);
                    long diff = lw ^ rw;

                    if (diff != 0) {
                        if (!e) {
                            return lessThanUnsigned(lw, rw) ? -1 : 1;
                        }

                        // Use binary search
                        int n = 0;
                        int y;
                        int x = (int) diff;
                        if (x == 0) {
                            x = (int) (diff >>> 32);
                            n = 32;
                        }

                        y = x << 16;
                        if (y == 0) {
                            n += 16;
                        } else {
                            x = y;
                        }

                        y = x << 8;
                        if (y == 0) {
                            n += 8;
                        }
                        return (int) (((lw >>> n) & 0xFFL) - ((rw >>> n) & 0xFFL));
                    }
                }

                // The epilogue to cover the last (minLength % 8) elements.
                for (int i = minWords * Longs.BYTES; i < minLength; i++) {
                    int result = UnsignedBytes.compare(
                            buffer1[offset1 + i],
                            buffer2[offset2 + i]);
                    if (result != 0) {
                        return result;
                    }
                }
                return length1 - length2;
            }
        }
    }
}

class Utf8_apache implements Comparable<Utf8>, CharSequence {

    private static final byte[] EMPTY = new byte[0];
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private byte[] bytes = EMPTY;
    private int length;
    private String string;

    public Utf8_apache() {
    }

    public Utf8_apache(String string) {
        this.bytes = getBytesFor(string);
        this.length = bytes.length;
        this.string = string;
    }

    public Utf8_apache(Utf8_apache other) {
        this.length = other.length;
        this.bytes = new byte[other.length];
        System.arraycopy(other.bytes, 0, this.bytes, 0, this.length);
        this.string = other.string;
    }

    public Utf8_apache(byte[] bytes) {
        this.bytes = bytes;
        this.length = bytes.length;
    }

    /**
     * Return UTF-8 encoded bytes. Only valid through {@link #getByteLength()}.
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Return length in bytes.
     *
     * @deprecated call {@link #getByteLength()} instead.
     */
    public int getLength() {
        return length;
    }

    /**
     * Return length in bytes.
     */
    public int getByteLength() {
        return length;
    }

    /**
     * Set length in bytes. Should called whenever byte content changes, even if
     * the length does not change, as this also clears the cached String.
     *
     * @deprecated call {@link #setByteLength(int)} instead.
     */
    public Utf8_apache setLength(int newLength) {
        return setByteLength(newLength);
    }

    /**
     * Set length in bytes. Should called whenever byte content changes, even if
     * the length does not change, as this also clears the cached String.
     */
    public Utf8_apache setByteLength(int newLength) {
        if (this.bytes.length < newLength) {
            byte[] newBytes = new byte[newLength];
            System.arraycopy(bytes, 0, newBytes, 0, this.length);
            this.bytes = newBytes;
        }
        this.length = newLength;
        this.string = null;
        return this;
    }

    /**
     * Set to the contents of a String.
     */
    public Utf8_apache set(String string) {
        this.bytes = getBytesFor(string);
        this.length = bytes.length;
        this.string = string;
        return this;
    }

    private abstract static class Utf8Converter {

        public abstract String fromUtf8(byte[] bytes, int length);

        public abstract byte[] toUtf8(String str);
    }

    private static final Utf8Converter UTF8_CONVERTER
            = System.getProperty("java.version").startsWith("1.6.")
                    ? new Utf8Converter() {                       // optimized for Java 6
                @Override
                public String fromUtf8(byte[] bytes, int length) {
                    try {
                        return new String(bytes, 0, length, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public byte[] toUtf8(String str) {
                    try {
                        return str.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            : new Utf8Converter() {                       // faster in Java 7 & 8
                @Override
                public String fromUtf8(byte[] bytes, int length) {
                    return new String(bytes, 0, length, UTF8);
                }

                @Override
                public byte[] toUtf8(String str) {
                    return str.getBytes(UTF8);
                }
            };

    @Override
    public String toString() {
        if (this.length == 0) {
            return "";
        }
        if (this.string == null) {
            this.string = UTF8_CONVERTER.fromUtf8(bytes, length);
        }
        return this.string;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Utf8)) {
            return false;
        }
        Utf8 that = (Utf8) o;
        if (!(this.length == that.length)) {
            return false;
        }
        byte[] thatBytes = that.bytes;
        for (int i = 0; i < this.length; i++) {
            if (bytes[i] != thatBytes[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < this.length; i++) {
            hash = hash * 31 + bytes[i];
        }
        return hash;
    }

    @Override
    public int compareTo(Utf8 that) {        
        throw new RuntimeException("_");
    //return BinaryData.compareBytes(this.bytes, 0, this.length,
        //                             that.bytes, 0, that.length);
    }

    // CharSequence implementation
    @Override
    public char charAt(int index) {
        return toString().charAt(index);
    }

    @Override
    public int length() {
        return toString().length();
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    /**
     * Gets the UTF-8 bytes for a String
     */
    public static final byte[] getBytesFor(String str) {
        return UTF8_CONVERTER.toUtf8(str);
    }

}
