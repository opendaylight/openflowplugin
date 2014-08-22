/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

/**
 * Provides an implementation of an integer based bitmap to enum-set
 * coder/decoder (codec) which can handle differences across OpenFlow versions.
 * This implementation is capable of handling a bit mask containing 32 bits.
 * <p>
 * The type parameter {@literal <E>} is an enumeration class that implements
 * the set of code values, derived from {@link OfpCodeBasedEnum}.  The code
 * values for the constants described by the enumeration class must be greater
 * than or equal to 0 and not exceed 31 for this codec to function correctly.
 * <p>
 * The code associated with the enumeration constant is used to indicate the
 * corresponding position in the encoded bitmap for that constant.  A value of
 * 1 in the bitmap in the position determined by the constant code indicates
 * that the flag is present.
 * <p>
 * For example, an enumeration constant has code value of 2. To determine if
 * this constant is set in the bit map, a value of 1 is shifted
 * left twice (1 &lt&lt 2).  If the bitmap has a 1 in this position,
 * then the flag is present.
 * <p>
 * Suppose you a have set of codes encoded as bits in a field; in OpenFlow
 * version 1.0:
 * <ul>
 *     <li>FOO = {@code 0} (treated as 1 &lt&lt 0)</li>
 *     <li>GOO = {@code 1} (treated as 1 &lt&lt 1)</li>
 *     <li>ZOO = {@code 2} (treated as 1 &lt&lt 2)</li>
 * </ul>
 * where version 1.1 added another code:
 * <ul>
 *     <li>BAR = {@code 4} (treated as 1 &lt&lt 4)</li>
 * </ul>
 * and in version 1.3 removed GOO and added BAZ, BOP and BAM:
 * <ul>
 *     <li>BAZ = {@code 5} (treated as 1 &lt&lt 5)</li>
 *     <li>BOP = {@code 6} (treated as 1 &lt&lt 6)</li>
 *     <li>BAM = {@code 7} (treated as 1 &lt&lt 7)</li>
 * </ul>
 * Then the set of valid bit positions for the different versions of the
 * protocol should be encoded in bit masks like this:
 * <pre>
 *     private static final int[] MASKS = {
 *         0x07,    // 1.0  (0000 0111)
 *         0x17,    // 1.1  (0001 0111)
 *         0x17,    // 1.2  (0001 0111)
 *         0xf5,    // 1.3  (1111 0101)
 *     };
 * </pre>
 * The code-based enumeration class should be written something like this:
 * <pre>
 * public enum FooBar implements OfpCodeEnum {}
 *     FOO(0),
 *     GOO(1),
 *     ZOO(2),
 *     BAR(4),
 *     BAZ(5),
 *     BOP(6),
 *     BAM(7),
 *     ;
 *
 *     private int code;
 *
 *     FooBar(int code) {
 *         this.code = code;
 *     }
 *
 *     private static final int[] MASKS = {
 *         0x07,    // 1.0
 *         0x17,    // 1.1
 *         0x17,    // 1.2
 *         0xf5,    // 1.3
 *     }
 *
 *     &#64;Override
 *     public int getCode(ProtocolVersion pv) {
 *         return code;
 *     }
 *
 *     // secret decoder ring
 *     private static final OfpCodeBasedCodec&lt;FooBar&gt; CODEC =
 *         new OfpCodeBasedCodec&lt;FooBar&gt;(MASKS, values);
 *
 *     // (javadocs go here)
 *     public static Set&lt;FooBar&gt; decodeFlags(int bitmap, ProtocolVersion pv) {
 *         return CODEC.decode(bitmap, pv);
 *     }
 *
 *     // (javadocs go here)
 *     public static int encodeFlags(Set&lt;FooBar&gt; flags, ProtocolVersion pv) {
 *         return CODEC.encode(flags, pv);
 *     }
 * </pre>
 *
 * @param <E> type of code-based enumeration
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public class OfpCodeBasedCodec<E extends OfpCodeBasedEnum>
        extends AbstractBitCodec<E> {

    private static final int MIN_BIT_POSITION = 0;
    private static final int MAX_BIT_POSITION = 31;

    /**
     * Constructs the codec, which squirrels away the bit masks and flag set
     * for use during the {@link #encode} and {@link #decode} methods.
     * Note that, most of the time, the flag set should be
     * {@code EnumClass.values()}.
     *
     * @param masks the "valid bit position" masks; one for each
     *              protocol version
     * @param flagSet the set of flags to iterate over during decode
     * @throws NullPointerException if either parameter is null
     * @throws IllegalArgumentException if masks does not include a mask for
     *         every protocol version or flagSet is not valid for this codec
     */
    public OfpCodeBasedCodec(int[] masks, E[] flagSet) {
        super(masks, flagSet);
    }

    // === for validation of flags ...

    @Override
    protected int getValue(E flag, ProtocolVersion pv) {
        return flag.getCode(pv);
    }

    @Override
    protected boolean cannotBeMapped(int value) {
        // check to see if the value is within range...
        return value < MIN_BIT_POSITION || value > MAX_BIT_POSITION;
    }

    @Override
    protected String formatValue(int value) {
        return Integer.toString(value);
    }

    // === for encoding and decoding ...

    @Override
    protected int getBitToMap(E flag, ProtocolVersion pv) {
        int code = flag.getCode(pv);
        return code == NA_CODE ? NA_CODE : 1 << code;
    }
}
