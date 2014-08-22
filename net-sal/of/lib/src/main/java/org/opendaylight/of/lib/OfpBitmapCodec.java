/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.hex;

/**
 * Provides a default implementation of a bitmap to enum-set
 * coder/decoder (codec), which can handle differences across OpenFlow versions.
 * This implementation is capable of handling a bit mask containing 32 bits.
 * <p>
 * The type parameter {@literal <E>} is the enumeration class that implements
 * the set of bit values.
 * <p>
 * Suppose you have a set of values encoded as bits in a field; in OpenFlow
 * version 1.0:
 * <ul>
 *     <li>FOO = {@code 0x1}</li>
 *     <li>GOO = {@code 0x2}</li>
 *     <li>ZOO = {@code 0x4}</li>
 * </ul>
 * where version 1.1 added another bit value:
 * <ul>
 *     <li>BAR = {@code 0x8}</li>
 * </ul>
 * and version 1.3 removed GOO, but added BAZ:
 * <ul>
 *     <li>BAZ = {@code 0x10}</li>
 * </ul>
 * Then the set of valid bit positions for the different versions of the
 * protocol should be encoded in bit masks like this:
 * <pre>
 *     private static final int[] MASKS = {
 *         0x07,   // 1.0
 *         0x0f,   // 1.1
 *         0x0f,   // 1.2 (no change from 1.1)
 *         0x1d,   // 1.3
 *    };
 * </pre>
 * The enumeration class representing the field, should be written something
 * like this:
 * <pre>
 * public enum FooBar implements OfpBitmapEnum {
 *     FOO(0x1),
 *     GOO(0x2),
 *     ZOO(0x4),
 *     BAR(0x8),
 *     BAZ(0x10),
 *     ;
 *
 *     private int bit;
 *
 *     FooBar(int bit) {
 *         this.bit = bit;
 *     }
 *
 *     private static final int[] MASKS = {
 *         0x07,   // 1.0
 *         0x0f,   // 1.1
 *         0x0f,   // 1.2 (no change from 1.1)
 *         0x1d,   // 1.3
 *     }
 *
 *     &#64;Override
 *     public int getBit(ProtocolVersion pv) {
 *         return bit;
 *     }
 *
 *     // secret decoder ring
 *     private static final OfpBitmapCodec&lt;FooBar&gt; CODEC =
 *             new OfpBitmapCodec&lt;FooBar&gt;(MASKS, values());
 *
 *     // (javadocs go here)
 *     public static Set&lt;FooBar&gt; decodeBitmap(int bitmap, ProtocolVersion pv) {
 *         return CODEC.decode(bitmap, pv);
 *     }
 *
 *     // (javadocs go here)
 *     public static int encodeBitmap(Set&lt;FooBar&gt;, ProtocolVersion pv) {
 *         return CODEC.encode(flags, pv);
 *     }
 * }
 * </pre>
 * A couple of things to note:
 * <ul>
 *     <li>In the simple example above, the bit values for each constant
 *     remain the same across all versions, so the {@code pv} parameter
 *     is not used in the {@link OfpBitmapEnum#getBit getBit()}
 *     implementation. Sometimes, however, the bit value changes for the
 *     same flag; for example {@code PortFeature.COPPER} is encoded as
 *     {@code 0x80} in version 1.0, and as {@code 0x800} in versions 1.1,
 *     1.2 and 1.3.
 *     </li>
 *     <li>Most of the time, the flags to iterate across during decoding
 *     are precisely all the defined constants (i.e. the result of
 *     {@code values()}). But in a few cases, bits have been grouped
 *     together to represent mutually exclusive values; these bit positions
 *     should not be iterated over by the decoder, but handled separately.
 *     See {@code PortState} for an example of this.
 *     </li>
 * </ul>
 *
 * @param <E> type of the bitmap enumeration
 *
 * @author Simon Hunt
 */
public class OfpBitmapCodec<E extends OfpBitmapEnum>
        extends AbstractBitCodec<E> {

    private static final Integer[] VALID_VALUES = {
            0x0,
            0x1, 0x2, 0x4, 0x8,
            0x10, 0x20, 0x40, 0x80,
            0x100, 0x200, 0x400, 0x800,
            0x1000, 0x2000, 0x4000, 0x8000,
            0x10000, 0x20000, 0x40000, 0x80000,
            0x100000, 0x200000, 0x400000, 0x800000,
            0x1000000, 0x2000000, 0x4000000, 0x8000000,
            0x10000000, 0x20000000, 0x40000000, 0x80000000,
    };
    private static final Set<Integer> VALID_ENCODINGS =
            new HashSet<Integer>(Arrays.asList(VALID_VALUES));

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
     */
    public OfpBitmapCodec(int[] masks, E[] flagSet) {
        super(masks, flagSet);
    }

    @Override
    protected int getValue(E flag, ProtocolVersion pv) {
        return flag.getBit(pv);
    }

    @Override
    protected boolean cannotBeMapped(int value) {
        return !VALID_ENCODINGS.contains(value);
    }

    @Override
    protected String formatValue(int value) {
        return hex(value);
    }

    // === for encoding and decoding ...

    @Override
    protected int getBitToMap(E flag, ProtocolVersion pv) {
        return flag.getBit(pv);
    }
}
