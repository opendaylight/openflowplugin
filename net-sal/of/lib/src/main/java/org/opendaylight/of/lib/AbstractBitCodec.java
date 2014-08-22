/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.util.ResourceUtils;

import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import static org.opendaylight.of.lib.CommonUtils.hex;
import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.msg.MessageFactory.isStrictMessageParsing;

/**
 * Provides a common base for the two bitmap codec classes.
 *
 * @param <E> type of enumeration
 *
 * @author Simon Hunt
 */
public abstract class AbstractBitCodec<E extends OfpEnum> {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            AbstractBitCodec.class, "abstractBitCodec");

    private static final String E_MASK_PER_VERSION = RES
            .getString("e_mask_per_version");
    private static final String E_NO_FLAGS = RES.getString("e_no_flags");
    private static final String E_CANNOT_BE_MAPPED = RES
            .getString("e_cannot_be_mapped");
    private static final String E_NA_FLAG = RES.getString("e_na_flag");
    private static final String E_NULL_PV = RES.getString("e_null_pv");
    private static final String E_BAD_BITS = RES.getString("e_bad_bits");
    private static final String COLON = " : ";

    /**
     * The value used to indicate that a flag is not applicable (for a given
     * protocol version.
     */
    protected static final int NA_CODE = -1;

    /** Bit masks indicating valid bit placements for each protocol version. */
    protected final int[] masks;
    /** The union of all flags that can be bitmapped, across all versions. */
    protected final E[] flagSet;

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
    public AbstractBitCodec(int[] masks, E[] flagSet) {
        notNull(masks, flagSet);
        if (masks.length != ProtocolVersion.values().length)
            throw new IllegalArgumentException(E_MASK_PER_VERSION);
        if (flagSet.length < 1)
            throw new IllegalArgumentException(E_NO_FLAGS);
        initStorage();
        validateFlags(flagSet);
        disposeStorage();
        this.masks = masks.clone();
        this.flagSet = flagSet.clone();
    }

    // iterate over the protocol version / flagset cross-product to validate
    private void validateFlags(E[] flagSet) {
        for (ProtocolVersion pv: ProtocolVersion.values()) {
            for (E flag: flagSet) {
                try {
                    int value = getValue(flag, pv);
                    if (value == NA_CODE)
                        return;

                    if (cannotBeMapped(value))
                        throw new IllegalArgumentException(E_CANNOT_BE_MAPPED +
                                flag + COLON + formatValue(value));

                } catch (VersionMismatchException e) {
                    // consume it; the flag is not defined for this version
                }
            }
        }
    }

    /**
     * Gives subclasses an opportunity to initialize temporary storage before
     * validation of the flag set. This default implementation does nothing.
     */
    protected void initStorage() { }

    /**
     * Gives subclasses an opportunity to prepare temporary storage for
     * garbage collection. This default implmentation does nothing.
     */
    protected void disposeStorage() { }

    /**
     * Returns the value (bit, or code) from the given flag, for the
     * given version.
     *
     * @param flag the flag
     * @param pv the protocol version
     * @return the bit value, or code value
     */
    protected abstract int getValue(E flag, ProtocolVersion pv);

    /**
     * This test should return true if the given value (code, or bit) cannot
     * be mapped into the bitmap; false if it is okay.
     *
     * @param value the value to test
     * @return true if unmappable; false otherwise
     */
    protected abstract boolean cannotBeMapped(int value);

    /**
     * Returns the appropriate formatting of the value for an exception message.
     *
     * @param value the value
     * @return the formatted value
     */
    protected abstract String formatValue(int value);

    /**
     * Decodes the given int value into a set of flags from the enumeration
     * class associated with this codec instance. If the message library has
     * been configured for {@link MessageFactory#setStrictMessageParsing(boolean)
     * strict parsing}, an exception will be thrown if a 1-bit is discovered in
     * an invalid position. If non-strict parsing is in effect, such invalid
     * bits will be ignored silently.
     *
     * @param bitmap the encoded bitmap
     * @param pv the protocol version
     * @return a set of flags corresponding to set bit positions in the bitmap
     * @throws NullPointerException if pv is null
     * @throws VersionMismatchException if a bit position is not supported
     *          in the given version, and strict parsing is enabled
     */
    public Set<E> decode(int bitmap, ProtocolVersion pv) {
        int mappedBits = bitmap;
        int mask = masks[pv.ordinal()];

        if (isStrictMessageParsing()) {
            if ((~mask & mappedBits) != 0)
                throw new VersionMismatchException(pv + E_BAD_BITS + hex(bitmap));
        } else {
            mappedBits = bitmap & mask;
        }

        Set<E> result = new TreeSet<E>();
        for (E flag: flagSet) {
            int bit = getBitToMap(flag, pv);
            if (bitPresentInBitmap(bit, mappedBits))
                result.add(flag);
        }
        return result;
    }

    private boolean bitPresentInBitmap(int bit, int bitmap) {
        return bit != NA_CODE && (bit & bitmap) != 0;
    }

    /**
     * Given a flag and a protocol version, this method should return the
     * appropriate bit in the map that represents this flag.
     *
     * @param flag the flag
     * @param pv the protocol version
     * @return the corresponding bit
     */
    protected abstract int getBitToMap(E flag, ProtocolVersion pv);

    /**
     * Encodes the given set of flags into a bitmap represented by an integer
     * value. The {@code flags} parameter may be empty or null, in which case
     * a bitmap of 0 (zero) is returned.
     *
     * @param flags the set of flags to be encoded
     * @param pv the protocol version
     * @return a bitmap corresponding to the given set of flags
     * @throws NullPointerException if protocol version is null
     * @throws VersionMismatchException if a flag is present that is not
     *          supported in the given protocol version
     * @throws IllegalArgumentException if a flag is present that cannot be
     *          mapped into the bitmap
     */
    public int encode(Set<E> flags, ProtocolVersion pv) {
        if (pv == null)
            throw new NullPointerException(E_NULL_PV);
        if (flags == null)
            return 0;

        int mask = masks[pv.ordinal()];
        int bitmap = 0;
        for (E flag: flags) {
            int value = getValue(flag, pv);
            if (value == NA_CODE)
                throw new VersionMismatchException(pv + E_NA_FLAG + flag);

            if (cannotBeMapped(value))
                throw new IllegalArgumentException(E_CANNOT_BE_MAPPED +
                        flag + COLON + formatValue(value));

            int bit = getBitToMap(flag, pv);
            if ((bit & mask) == 0)
                throw new VersionMismatchException(pv + E_NA_FLAG + flag);

            bitmap |=  bit;
        }
        return bitmap;
    }
}