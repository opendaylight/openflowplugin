/*
 * (c) Copyright 2010-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Random;

import static org.opendaylight.util.StringUtils.EOL;

/**
 * Generates byte arrays, either randomly, or via an iterator.
 * The possible values that the arrays are initialized with are constrained
 * by a "specification" that describes what values each
 * element of the array can possibly take.
 * <p>
 * Instances of this class are created via the factory methods {@link #create}
 * and {@link #createFromHex}. Once you have an instance, each call
 * to {@link #generate} will create a newly instantiated byte array
 * initialized randomly as per the construction specification.
 * <p>
 * Given an instance, you can ask it to create a byte array with
 * the {@link #lowest} or {@link #highest} values defined by the specification,
 * as well as ask {@link #resultSpaceSize() how many} possible arrays could
 * be generated. You can also obtain an {@link #iterator} which will iterate
 * from the lowest to highest values defined by the specification.
 * <p>
 * Given two instances, you can see whether one is a
 * {@link #isSuperset superset} of the other, or if the
 * two {@link #intersects intersect}.
 *
 * @author Simon Hunt
 */
public class ByteArrayGenerator {

    private static final String RE_0_TO_255 = "([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
    private static final String RE_BYTE_LOC =
            "(" + RE_0_TO_255 + "(-" + RE_0_TO_255 + ")?)";
    private static final String RE_BYTE_LOC_STAR = "(" + RE_BYTE_LOC + "|\\*)";
    private static final String RE_SPEC =
            RE_BYTE_LOC_STAR + "(:" + RE_BYTE_LOC_STAR + ")*";

    private static final String RE_RANGE = RE_0_TO_255 + "-" + RE_0_TO_255;

    private static final String RE_00_TO_FF = "([0-9a-fA-F][0-9a-fA-F]?)";
    private static final String RE_BYTE_LOC_HEX =
            "(" + RE_00_TO_FF + "(-" + RE_00_TO_FF + ")?)";
    private static final String RE_BYTE_LOC_HEX_STAR =
            "(" + RE_BYTE_LOC_HEX + "|\\*)";
    private static final String RE_SPEC_HEX =
            RE_BYTE_LOC_HEX_STAR + "(:" + RE_BYTE_LOC_HEX_STAR + ")*";

    private static final String RE_RANGE_HEX = RE_00_TO_FF + "-" + RE_00_TO_FF;

    private static final String STAR = "*";

    static final String E_MAL = "Malformed Spec: ";
    static final String EX_HEX = " (expecting Hex)";
    static final String EX_DEC = " (expecting Dec)";
    static final String E_LOHI = "Low/High error: ";


    /** Differentiates between fixed byte values and range byte values. */
    private static enum ByteType { FIXED, RANGE }

    /** Differentiates between the low and high values. */
    private static enum Which { LOW, HIGH }

    /** Differentiates between decimal and hex. */
    private static enum Radix { DECIMAL, HEX }


    private static final Random RANDOM = new Random();
    private static final String COLON = ":";
    private static final String DASH = "-";

    private final String spec;
    private final String normalizedSpec;
    private final BigInteger resultSpaceSize;
    final ByteType[] byteTypes;
    final int[] lows;
    final int[] highs;
    final int[] windows;

    /** private constructor.
     *
     * @param spec the specification
     * @param r whether the bytes should be parsed as Decimal or Hex
     */
    private ByteArrayGenerator(String spec, Radix r) {
        final boolean inHex = r==Radix.HEX;
        if (spec == null)
            throw new NullPointerException("spec cannot be null");
        if (!spec.matches(inHex ? RE_SPEC_HEX : RE_SPEC)) {
            String exp = inHex ? EX_HEX : EX_DEC;
            throw new IllegalArgumentException(E_MAL + "["+spec+"]" + exp);
        }
        this.spec = spec;
        String[] byteSpecs = spec.split(COLON);
        final int nBytes = byteSpecs.length;
        final int radix = inHex ? 16 : 10;
        byteTypes = new ByteType[nBytes];
        lows = new int[nBytes];
        highs = new int[nBytes];
        windows= new int[nBytes];
        StringBuilder normalized = new StringBuilder();

        int i=0;
        BigInteger spaceSize = BigInteger.ONE;
        for (String s: byteSpecs) {
            if (s.matches(inHex ? RE_RANGE_HEX : RE_RANGE)) {
                byteTypes[i] = ByteType.RANGE;
                String[] vals = s.split(DASH);
                int low = Integer.parseInt(vals[0], radix);
                int high = Integer.parseInt(vals[1], radix);
                if (low >= high)
                    throw new IllegalArgumentException(E_MAL + E_LOHI +
                            "[" + s + "]");
                lows[i] = low;
                highs[i] = high;
                windows[i] = highs[i] - lows[i] + 1;
            } else {
                // must be fixed value (or STAR)
                if (STAR.equals(s)) {
                    // STAR represents the complete range 0-255
                    byteTypes[i] = ByteType.RANGE;
                    lows[i] = 0;
                    highs[i] = 255;
                    windows[i] = 256;
                } else {
                    byteTypes[i] = ByteType.FIXED;
                    int val = Integer.parseInt(s, radix);
                    lows[i] = val;
                    highs[i] = val;
                    windows[i] = 1;
                }
            }
            spaceSize = spaceSize.multiply(BigInteger.valueOf(windows[i]));
            if (i > 0)
                normalized.append(COLON);
            normalized.append(makeByteSpec(lows[i], highs[i]));
            i++;
        }
        resultSpaceSize = spaceSize;
        normalizedSpec = normalized.toString();
    }

    /** Creates a consistent specification for a single byte.
     *
     * @param low the low value of this byte position
     * @param high the high value of this byte position
     * @return the normalized spec for this byte position
     */
    private static String makeByteSpec(int low, int high) {
        if (low == high)
            return String.format("%02x", low);
        else if (low == 0 && high == 255)
            return STAR;
        else
            return String.format("%02x-%02x", low, high);
    }

    /** Returns the "size" of this generator; that is, how many bytes in each
     * generated array.
     *
     * @return the size
     */
    public int size() {
        return byteTypes.length;
    }

    /** Returns the specification string with which this generator
     * was initialized.
     *
     * @return the spec
     */
    public String getSpec() {
        return spec;
    }


    /** Returns a normalized specification string; that is to say, the
     * bytes are expressed in lowercase hex, and instances of {@code 00-ff}
     * are replaced with {@code '*'}. For example, the generators created
     * with the following:
     * <pre>
     * create("254:12:0-255:0-255")
     * createFromHex("FE:0C:00-FF:00-FF")
     * createFromHex("fe:c:*:*")
     * </pre>
     * all return the same (normalized) byte spec:
     * <pre>
     * "fe:0c:*:*"
     * </pre>
     *
     * @return the normalized byte specification string
     */
    public String getNormalizedSpec() {
        return normalizedSpec;
    }

    /** Creates and returns a new byte array populated with random data as
     * per the specification.
     *
     * @return a new byte array with pseudo-random data in it
     */
    public byte[] generate() {
        final int size = size();
        byte[] array = new byte[size];
        for (int i = 0; i<size; i++) {
            array[i] = genByte(i);
        }
        return array;
    }

    /** Private helper method to create a random byte for array element.
     *
     * @param idx the index of the array element
     * @return a random byte as per the spec for the indexed element
     */
    private byte genByte(int idx) {
        switch (byteTypes[idx]) {
            default:
            case FIXED:
                return (byte) lows[idx];

            case RANGE:
                return (byte) (RANDOM.nextInt(windows[idx]) + lows[idx]);
        }
    }

    /** Returns an array with the byte values set to their lowest values.
     *
     * @return an array with the lowest values
     */
    public byte[] lowest() {
        return rangeEnd(Which.LOW);
    }

    /** Returns an array with the byte values set to their highest values.
     *
     * @return an array with the highest values
     */
    public byte[] highest() {
        return rangeEnd(Which.HIGH);
    }

    /** Returns an array filled with either the lowest or highest values.
     *
     * @param which do you want to go low or go high?
     * @return the appropriate array
     */
    private byte[] rangeEnd(Which which) {
        final int size = size();
        byte[] array = new byte[size];
        for (int i=0; i<size; i++) {
            array[i] = which == Which.LOW ? (byte) lows[i] : (byte) highs[i];
        }
        return array;
    }

    /** Returns the total number of possible arrays that could be generated
     * by this array generator.
     *
     * @return the total number of possible arrays
     */
    public BigInteger resultSpaceSize() {
        return resultSpaceSize;
    }

    /** Returns an iterator that starts with the lowest specified value for
     * the byte array and iterates through to the highest specified value,
     * returning {@link #resultSpaceSize} byte arrays in total.
     *
     * @return an iterator
     */
    public Iterator<byte[]> iterator() {
        return new Iter();
    }

    @Override
    public String toString() {
        return "[ByteArrayGenerator: " + normalizedSpec + "]";
    }

    /** Returns a multi-line string representation with information
     * suitable for debugging.
     *
     * @return a multi-line string representation
     */
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString()).append(EOL);
        sb.append(" Original Spec: ").append(spec).append(EOL);
        sb.append(" Size: ").append(size()).append(EOL);
        sb.append(" Result Space Size: ").append(resultSpaceSize()).append(EOL);
        sb.append(" Lowest: ").append(ByteUtils.toHexString(lowest())).append(EOL);
        sb.append(" Highest: ").append(ByteUtils.toHexString(highest())).append(EOL);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ByteArrayGenerator that = (ByteArrayGenerator) o;
        return normalizedSpec.equals(that.normalizedSpec);
    }

    @Override
    public int hashCode() {
        return normalizedSpec.hashCode();
    }

    /** Ensures that the other generator is the same size as this one.
     *
     * @param other the other generator
     * @return the size (for convenience)
     */
    private int validateForCompare(ByteArrayGenerator other) {
        if (other == null)
            throw new NullPointerException("other cannot be null");
        final int sz = size();
        if (other.size() != sz)
            throw new IllegalArgumentException("size mismatch! expected <" +
                    sz + "> but was <" + other.size() + ">");
        return sz;
    }

    /** Returns {@code true} if <em>this</em> byte array generator is a
     * superset of the <em>other</em> byte array generator; false otherwise.
     * Put another way, the set of all possible byte arrays produced by the
     * <em>other</em> generator is a subset of the set of all possible byte
     * arrays produced by <em>this</em> generator.
     *
     * @param other the other generator to compare with
     * @return true if this generator is a superset of the other generator;
     *          false otherwise
     * @throws IllegalArgumentException if other generator is not the same
     *          size as this one
     * @throws NullPointerException if other is null
     */
    public boolean isSuperset(ByteArrayGenerator other) {
        final int sz = validateForCompare(other);
        // see if we can find something in 'other' that falls outside the
        // range of 'this'
        for (int i=0; i<sz; i++) {
            if (other.lows[i] < this.lows[i] || other.highs[i] > this.highs[i])
                return false;
        }
        // other generator lays completely within the bounds of this generator
        return true;
    }

    /** Returns {@code true} if the specified byte array can be generated
     * by <em>this</em> byte array generator.
     *
     * @param b the byte array
     * @return true if this generator can generate a byte array with this
     *          value; false otherwise
     */
    public boolean contains(byte[] b) {
        if (b == null)
            throw new NullPointerException("parameter cannot be null");
        final int sz = size();
        if (b.length != sz)
            throw new IllegalArgumentException("size mismatch! expected <" +
                    sz + "> but was <" + b.length + ">");
        for (int i=0; i<sz; i++) {
            if (ByteUtils.byteToInt(b[i]) < lows[i] || ByteUtils.byteToInt(b[i]) > highs[i])
                return false;
        }
        // every byte within specified range for its position in the array
        return true;
    }

    /** Returns {@code true} if <em>this</em> byte array generator intersects
     * the <em>other</em> byte array generator; false otherwise.
     * Put another way, this predicate returns true if the set of all possible
     * byte arrays produced by <em>this</em> generator contains <u>any</u> byte
     * array that could possibly be generated by the <em>other</em> generator.
     *
     * @param other the other generator to compare with
     * @return true if this generator intersects the other generator; false
     *          otherwise
     * @throws IllegalArgumentException if the other generator is not the
     *          same size as this one
     * @throws NullPointerException if other is null
     */
    public boolean intersects(ByteArrayGenerator other) {
        final int sz = validateForCompare(other);
        // if a byte position does not intersect,
        // then the whole generator does not intersect
        for (int i=0; i<sz; i++) {
            if (other.lows[i] > this.highs[i] || other.highs[i] < this.lows[i])
                return false;
        }
        return true;
    }



    //=== Public API =========================================================

    /** Creates an instance of {@link ByteArrayGenerator} that will generate
     * byte arrays within the constraints defined by the given specification.
     * Specifications are declared with decimal values.
     * <p>
     * <em>spec</em> is a colon delimited string of the following form:
     * <pre>
     *  {byteSpec} [ : {byteSpec} [ ... ] ]
     * </pre>
     * where <em>byteSpec</em> represents a single byte in the array, and is
     * of the form:
     * <pre>
     * {n} | {n}-{m} | *
     * </pre>
     * where <em>n</em> and <em>m</em> are integers in the range 0 to 255,
     * <em>m</em> (when specified) is &gt; <em>n</em>, and <em>*</em>
     * represents <em>0-255</em>.
     * <p>
     * For example, the specification {@code "15:1:0-30:100-255"} declares that
     * the generator should return byte arrays 4 bytes in length, where the
     * first byte (index = 0) is always 15, the second byte (index = 1) is
     * always 1, the third byte is drawn randomly from the range 0 .. 30, and
     * the last byte is drawn randomly from the range 100 .. 255.
     * <p>
     * Another example, the specification {@code "0-255:0-255"} declares that
     * the generator should return byte arrays 2 bytes in length, with both
     * bytes being any randomly chosen value. This specification could also be
     * written {@code "*:*"}
     *
     * @param spec the specification
     * @return a byte array generator
     * @throws IllegalArgumentException if spec is ill-formed
     */
    public static ByteArrayGenerator create(String spec) {
        return new ByteArrayGenerator(spec, Radix.DECIMAL);
    }

    /** Creates an instance of {@link ByteArrayGenerator} that will generate
     * byte arrays within the constraints defined by the given specification.
     * Specifications are declared with hex values.
     * <p>
     * <em>Spec</em> is a colon delimited string of the following form:
     * <pre>
     *  {byteSpec} [ : {byteSpec} [ ... ] ]
     * </pre>
     * where <em>byteSpec</em> represents a single byte in the array, and is
     * of the form:
     * <pre>
     * {n} | {n}-{m} | *
     * </pre>
     * where <em>n</em> and <em>m</em> are hex values in the range 0 to FF,
     * <em>m</em> (when specified) is &gt; <em>n</em>, and <em>*</em>
     * represents {@code 0-FF}. Upper- or lower- case hex digits are allowed.
     * <p>
     * For example, the specification {@code "F:1:0-1E:C8-FF"} declares that
     * the generator should return byte arrays 4 bytes in length, where the
     * first byte (index = 0) is always F (15), the second byte (index = 1)
     * is always 1, the third byte is drawn randomly from the range 0 .. 1E
     * (0 .. 30), and the last byte is drawn randomly from the range
     * C8 .. FF (200 .. 255).
     * <p>
     * Another example, the specification {@code "00-FF:00-FF"} declares that
     * the generator should return byte arrays 2 bytes in length, with both
     * bytes being any randomly chosen value. This specification could also
     * be written {@code "*:*"}
     *
     * @param spec the specification
     * @return a byte array generator
     * @throws IllegalArgumentException if spec is ill-formed
     */
    public static ByteArrayGenerator createFromHex(String spec) {
        return new ByteArrayGenerator(spec, Radix.HEX);
    }



    //==== Iterator ==========================================================

    private class Iter implements Iterator<byte[]> {

        private int[] current;
        private boolean last = false;
        private boolean more = true;

        private Iter() {
            // start at the lowest value
            current = new int[lows.length];
            System.arraycopy(lows, 0, current, 0, lows.length);
        }

        private void incrementCurrent() {
            int idx = current.length - 1;
            boolean rollover = true;
            while (rollover && idx >= 0) {
                if (ByteArrayGenerator.this.byteTypes[idx] == ByteType.FIXED) {
                    idx--;
                } else {
                    current[idx]++;
                    if (current[idx] > highs[idx]) {
                        current[idx] = lows[idx];
                        idx--;
                    } else {
                        rollover = false;
                    }
                }
            }
        }

        @Override
        public boolean hasNext() {
            if (!more) return false;

            int result = 0; // == maybe; 1 == yes; -1 == no
            int idx = 0;
            while (result == 0 && idx < current.length) {
                if (current[idx] > highs[idx]) {
                    result = -1; // no more
                } else if (current[idx] < highs[idx]) {
                    result = 1; // obviously
                } else {
                    idx++;
                }
            }
            last = result == 0;
            return result > -1;
        }

        @Override
        public byte[] next() {
            if (!hasNext()) return null;

            byte[] copy = new byte[current.length];
            for (int i=0; i<copy.length; i++) {
                copy[i] = ByteUtils.intToByte(current[i]);
            }
            if (!last) {
                incrementCurrent();
            } else {
                more = false;
            }
            return copy;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
