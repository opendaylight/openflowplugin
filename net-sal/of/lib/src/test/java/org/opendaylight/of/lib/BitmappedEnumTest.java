/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Base class for unit tests of bitmapped enums.
 *
 * @param <T> type of the enumeration
 *
 * @author Simon Hunt
 */
public abstract class BitmappedEnumTest<T extends Enum<?>> extends AbstractTest {

    private static final String E_BAD_BITS = " (strict decode) Bad bits: 0x";
    private static final String E_BAD_FLAG = " Flag not defined for version: ";

    /** Verifies that, for a given protocol version, a specific bit
     * position and its associated flag throws {@link VersionMismatchException}
     * when decoded/encoded.
     * <p>
     * For example:
     * <pre>
     *     verifyNaBit(ProtocolVersion.V_1_0, 0x40, SomeEnum.CONSTANT);
     * </pre>
     *
     * @param pv the protocol version
     * @param bit the bit
     * @param flag the flag
     * @param chkMsgPrefix if true, the exception message prefix is tested
     */
    protected void verifyNaBit(ProtocolVersion pv, int bit, T flag,
                               boolean chkMsgPrefix) {
        String ePrefix = pv + E_BAD_BITS;
        String ePrefix2 = pv + E_BAD_FLAG;
        try {
            decodeBitmap(bit, pv);
            if (MessageFactory.isStrictMessageParsing())
                fail(AM_NOEX);
        } catch (VersionMismatchException vme) {
            print(FMT_EX, vme);
            if (chkMsgPrefix)
                assertTrue(AM_HUH, vme.getMessage().startsWith(ePrefix));
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }

        // check that attempting to encode the flag throws an exception
        if (flag != null) {
            Set<T> flags = new HashSet<T>();
            flags.add(flag);
            try {
                encodeBitmap(flags, pv);
                fail(AM_NOEX);
            } catch (VersionMismatchException vme) {
                print(FMT_EX, vme);
                if (chkMsgPrefix)
                    assertTrue(AM_HUH, vme.getMessage().startsWith(ePrefix2));
            } catch (Exception e) {
                print(e);
                fail(AM_WREX);
            }
        }
    }

    protected void verifyNaBit(ProtocolVersion pv, int bit, T flag) {
        verifyNaBit(pv, bit, flag, true);
    }

    protected void verifyNaBit(ProtocolVersion pv, int bit) {
        verifyNaBit(pv, bit, null, true);
    }

    /** Verifies that, for a given protocol version, all (U16) bits starting at
     * the specified position and moving left (more significant) will throw
     * a {@link VersionMismatchException}.
     * <p>
     * For example:
     * <pre>
     *     verifyNaU16(ProtocolVersion.V_1_0, 0x100);
     * </pre>
     * Verifies that bits {@code 0x0100} through {@code 0x8000}
     * will throw an exception.
     *
     * @param pv the protocol version
     * @param startBit the bit from which to start
     * @param chkMsgPrefix if true, the exception message prefix is tested
     */
    protected void verifyNaU16(ProtocolVersion pv, int startBit,
                               boolean chkMsgPrefix) {
        verifyNa(pv, startBit, 0x10000, chkMsgPrefix);
    }

    protected void verifyNaU16(ProtocolVersion pv, int startBit) {
        verifyNaU16(pv, startBit, true);
    }

    /** Verifies that, for a given protocol version, all (U32) bits starting at
     * the specified position and moving left (more significant) will throw
     * a {@link VersionMismatchException}.
     * <p>
     * For example:
     * <pre>
     *     verifyNaU32(ProtocolVersion.V_1_0, 0x200000);
     * </pre>
     * Verifies that bits {@code 0x00200000} through {@code 0x80000000}
     * will throw an exception.
     *
     * @param pv the protocol version
     * @param startBit the bit from which to start
     * @param chkMsgPrefix if true, the exception message prefix is tested
     */
    protected void verifyNaU32(ProtocolVersion pv, int startBit,
                               boolean chkMsgPrefix) {
        verifyNa(pv, startBit, 0x100000000L, chkMsgPrefix);
    }

    protected void verifyNaU32(ProtocolVersion pv, int startBit) {
        verifyNaU32(pv, startBit, true);
    }

    /** Verifies that, for a given protocol version, all bits starting at
     * the specified position and moving left (more significant), until
     * reaching the specified end bit, will throw
     * a {@link VersionMismatchException}.
     *
     * @param pv the protocol version
     * @param startBit the bit from which to start
     * @param endBit the bit at which to stop
     * @param chkMsgPrefix if true, the exception message prefix is tested
     */
    private void verifyNa(ProtocolVersion pv, int startBit, long endBit,
                          boolean chkMsgPrefix) {
        long bit = startBit;
        do {
            verifyNaBit(pv, (int)bit, null, chkMsgPrefix);
            bit <<= 1;
        } while (bit != 0 && bit < endBit);
    }

    /** Verifies that, for a given protocol version, the specified bit position
     * decodes to the specified flag.
     *
     * @param pv the protocol version
     * @param bit the bit to test
     * @param expFlag the expected corresponding flag
     */
    protected void verifyBit(ProtocolVersion pv, int bit, T expFlag) {
        verifyBit(pv, bit, expFlag, true);
    }

    /** Verifies that, for a given protocol version, the specified bit position
     * decodes to the specified flag. If the strict parameter is true, the
     * decoded result set must contain one, and only one, flag. To relax this
     * restriction, set strict to false. This is sometimes needed when
     * flag constants are "generated" by the absence of bits. See
     * {@code ConfigFlagTest} for an example of this.
     *
     * @param pv the protocol version
     * @param bit the bit to test
     * @param expFlag the expected corresponding flag
     * @param strict assert that only one flag should be in the result set
     */
    protected void verifyBit(ProtocolVersion pv, int bit, T expFlag,
                             boolean strict) {
        Set<T> flags = decodeBitmap(bit, pv);
        print(FMT_PV_BITS_FLAGS, pv, hex(bit), flags);
        if (strict)
            assertEquals(AM_UXS, 1, flags.size());
        assertTrue("wrong flag", flags.contains(expFlag));

        // now verify the encoding of the flag
        int bitmap = encodeBitmap(flags, pv);
        print(FMT_FLAGS_BITS, flags, hex(bitmap));
        assertEquals(AM_NEQ, bit, bitmap);
    }

    /** Verifies that the given set contains exactly the enumerations
     * specified as the remaining arguments.
     *
     * @param pv the protocol version
     * @param bits the bitmapped value
     * @param exp the expected constants
     */
    protected void verifyBitmappedFlags(ProtocolVersion pv, int bits, T... exp) {
        Set<T> flags = decodeBitmap(bits, pv);
        print(FMT_PV_BITS_FLAGS, pv, hex(bits), flags);
        for (T t: exp)
            assertTrue("missing flag: " + t, flags.contains(t));
        assertEquals(AM_UXS, exp.length, flags.size());

        // now verify the encoding of the flags
        int bitmap = encodeBitmap(flags, pv);
        print(FMT_FLAGS_BITS, flags, hex(bitmap));
        assertEquals(AM_NEQ, bits, bitmap);
    }


    // for basic testing of the enum constants
    private static final int COL_WIDTH_NAME = 18;
    private static final int COL_WIDTH_DISPLAY = 38;
    private static final String LINE_NAME =
            StringUtils.pad("", COL_WIDTH_NAME, '-');
    private static final String LINE_DISPLAY =
            StringUtils.pad("", COL_WIDTH_DISPLAY, '-');
    protected static final String FMT_ENUM_STRINGS = "{} : {} : {}";
    private static final String DOT_NAME = padName(".name()");
    private static final String DOT_TO_STRING = padName(".toString()");
    private static final String DOT_TO_DISPLAY_STRING =
            padDisplay(".toDisplayString()");

    protected static final String BASIC_HEADER =
            EOL + StringUtils.format(FMT_ENUM_STRINGS,
                    DOT_NAME, DOT_TO_STRING, DOT_TO_DISPLAY_STRING) +
                    EOL + StringUtils.format(FMT_ENUM_STRINGS,
                    LINE_NAME, LINE_NAME, LINE_DISPLAY);

    protected static String padName(Object o) {
        return StringUtils.pad(o.toString(), COL_WIDTH_NAME);
    }

    protected static String padDisplay(String s) {
        return StringUtils.pad(s, COL_WIDTH_DISPLAY);
    }


    /** Subclasses should invoke the decodeBitmap() method of the target
     * enum and return the results.
     *
     * @param bitmap the bitmap to be decoded
     * @param pv the protocol version
     * @return the set of flags
     */
    protected abstract Set<T> decodeBitmap(int bitmap, ProtocolVersion pv);

    /** Subclasses should invoke the encodeBitmap() method of the target
     * enum and return the results.
     *
     * @param flags the flags to be encoded
     * @param pv the protocol version
     * @return the bitmap
     */
    protected abstract int encodeBitmap(Set<T> flags, ProtocolVersion pv);
}
