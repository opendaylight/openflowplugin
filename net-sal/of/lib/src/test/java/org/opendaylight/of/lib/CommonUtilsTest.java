/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for CommonUtils.
 *
 * @author Simon Hunt
 */
public class CommonUtilsTest extends AbstractTest {

    private static final String MSG = "Something";

    @Test
    public void testHex() {
        print(EOL + "hex()");
        assertEquals(AM_NEQ, "0x30", CommonUtils.hex(48));
        assertEquals(AM_NEQ, "0x40", CommonUtils.hex(64L));
    }

    @Test
    public void testParseHexInt() {
        assertEquals(AM_NEQ, 0x10, parseHexInt("0X10")); // uppercase
        assertEquals(AM_NEQ, 0x10, parseHexInt("0x10")); // lowercase
        assertEquals(AM_NEQ, 0x10, parseHexInt("10")); // no prefix
        assertEquals(AM_NEQ, -0x10, parseHexInt("-0x10")); // negative
        assertEquals(AM_NEQ, -0x10, parseHexInt("-10")); // negative no prefix
        assertEquals(AM_NEQ, Integer.MAX_VALUE,
                parseHexInt(Integer.toHexString(Integer.MAX_VALUE))); // max
        assertEquals(AM_NEQ, Integer.MAX_VALUE,
                parseHexInt("0x7fffffff")); // max prefix
        assertEquals(AM_NEQ, -0x7fffffff,
                     parseHexLong("-0x7fffffff")); // negative max
        try {
            parseHexInt("0xfoo"); // invalid hex
            fail(AM_NOEX);
        } catch (NumberFormatException e) {
            print(FMT_EX, e);
        }

        try {
            parseHexInt(hex(Long.MAX_VALUE)); // too large
            fail(AM_NOEX);
        } catch (NumberFormatException e) {
            print(FMT_EX, e);
        }
    }

    @Test
    public void testParseHexLong() {
        assertEquals(AM_NEQ, 0x10L, parseHexLong("0X10")); // uppercase
        assertEquals(AM_NEQ, 0x10L, parseHexLong("0x10")); // lowercase
        assertEquals(AM_NEQ, 0x10L, parseHexLong("10")); // no prefix
        assertEquals(AM_NEQ, -0x10L, parseHexLong("-0x10")); // negative
        assertEquals(AM_NEQ, -0x10L, parseHexLong("-10")); // negative no pre
        assertEquals(AM_NEQ, 0x7fffffffffffffffL,
                     parseHexLong("0x7fffffffffffffff")); // max prefix
        assertEquals(AM_NEQ, Long.MAX_VALUE,
                     parseHexLong(Long.toHexString(Long.MAX_VALUE))); // max
        assertEquals(AM_NEQ, 0xffffffffffffffffL,
                     parseHexLong("0xffffffffffffffff")); // unsigned max

        try {
            parseHexLong("0xfoo"); // invalid hex
            fail(AM_NOEX);
        } catch (NumberFormatException e) {
            print(FMT_EX, e);
        }

        try {
            parseHexLong(Double.toHexString(Double.MAX_VALUE)); // too large
            fail(AM_NOEX);
        } catch (NumberFormatException e) {
            print(FMT_EX, e);
        }
    }

    @Test
    public void testCSize() {
        print(EOL + "cSize()");
        Collection<Integer> c = null;
        assertEquals(AM_UXS, 0, cSize(c));
        c = new ArrayList<Integer>();
        assertEquals(AM_UXS, 0, cSize(c));
        c.add(42);
        c.add(69);
        assertEquals(AM_UXS, 2, cSize(c));
    }

    @Test
    public void testASize() {
        print(EOL + "testASize()");
        assertEquals(AM_UXS, 0, aSize((byte[])null));
        assertEquals(AM_UXS, 0, aSize(new byte[0]));
        assertEquals(AM_UXS, 7, aSize(new byte[7]));
        assertEquals(AM_UXS, 0, aSize((Object[])null));
        assertEquals(AM_UXS, 0, aSize(new Object[0]));
        assertEquals(AM_UXS, 7, aSize(new Object[7]));
    }

    private void verifyVerminGood(ProtocolVersion pv, String... expMsg) {
        if (expMsg[0] == null)
            print("{} -> Good", pv);
        else
            fail(AM_NOEX);
    }

    private void verifyVerminBad(VersionMismatchException vme, String... expMsg) {
        if (expMsg.length > 0) {
            print(FMT_EX, vme);
            String expText = expMsg.length > 1
                    ? expMsg[0] + " (" + expMsg[1] + ")" : expMsg[0];
            assertEquals(AM_NEQ, expText, vme.getMessage());
        } else
            fail(AM_UNEX);
    }

    private void verifyVermin11(ProtocolVersion pv, String expMsg) {
        try {
            verMin11(pv);
            verifyVerminGood(pv, expMsg);
        } catch (VersionMismatchException vme) {
            verifyVerminBad(vme, expMsg);
        }
    }

    private void verifyVermin12(ProtocolVersion pv, String expMsg) {
        try {
            verMin12(pv);
            verifyVerminGood(pv, expMsg);
        } catch (VersionMismatchException vme) {
            verifyVerminBad(vme, expMsg);
        }
    }

    private void verifyVermin13(ProtocolVersion pv, String expMsg) {
        try {
            verMin13(pv);
            verifyVerminGood(pv, expMsg);
        } catch (VersionMismatchException vme) {
            verifyVerminBad(vme, expMsg);
        }
    }

    private void verifyVermin11(ProtocolVersion pv, String expMsg, String s) {
        try {
            verMin11(pv, s);
            verifyVerminGood(pv, expMsg);
        } catch (VersionMismatchException vme) {
            verifyVerminBad(vme, expMsg, s);
        }
    }

    private void verifyVermin12(ProtocolVersion pv, String expMsg, String s) {
        try {
            verMin12(pv, s);
            verifyVerminGood(pv, expMsg);
        } catch (VersionMismatchException vme) {
            verifyVerminBad(vme, expMsg, s);
        }
    }

    private void verifyVermin13(ProtocolVersion pv, String expMsg, String s) {
        try {
            verMin13(pv, s);
            verifyVerminGood(pv, expMsg);
        } catch (VersionMismatchException vme) {
            verifyVerminBad(vme, expMsg, s);
        }
    }

    @Test
    public void testVerMin11() {
        print(EOL + "verMin11()");
        verifyVermin11(V_1_0, E_NOT_SUP_BEFORE_11);
        verifyVermin11(V_1_1, null);
        verifyVermin11(V_1_2, null);
        verifyVermin11(V_1_3, null);
    }

    @Test
    public void testVerMin12() {
        print(EOL + "verMin12()");
        verifyVermin12(V_1_0, E_NOT_SUP_BEFORE_12, MSG);
        verifyVermin12(V_1_1, E_NOT_SUP_BEFORE_12, MSG);
        verifyVermin12(V_1_2, null);
        verifyVermin12(V_1_3, null);
    }

    @Test
    public void testVerMin13() {
        print(EOL + "verMin13()");
        verifyVermin13(V_1_0, E_NOT_SUP_BEFORE_13);
        verifyVermin13(V_1_1, E_NOT_SUP_BEFORE_13);
        verifyVermin13(V_1_2, E_NOT_SUP_BEFORE_13);
        verifyVermin13(V_1_3, null);
    }

    @Test
    public void testVerMin11Text() {
        print(EOL + "verMin11Text()");
        verifyVermin11(V_1_0, E_NOT_SUP_BEFORE_11, MSG);
        verifyVermin11(V_1_1, null);
        verifyVermin11(V_1_2, null);
        verifyVermin11(V_1_3, null);
    }

    @Test
    public void testVerMin12Text() {
        print(EOL + "verMin12()Text");
        verifyVermin12(V_1_0, E_NOT_SUP_BEFORE_12, MSG);
        verifyVermin12(V_1_1, E_NOT_SUP_BEFORE_12, MSG);
        verifyVermin12(V_1_2, null);
        verifyVermin12(V_1_3, null);
    }

    @Test
    public void testVerMin13Text() {
        print(EOL + "verMin13()Text");
        verifyVermin13(V_1_0, E_NOT_SUP_BEFORE_13, MSG);
        verifyVermin13(V_1_1, E_NOT_SUP_BEFORE_13, MSG);
        verifyVermin13(V_1_2, E_NOT_SUP_BEFORE_13, MSG);
        verifyVermin13(V_1_3, null);
    }

    private void verifyVerMinSince(ProtocolVersion pv, ProtocolVersion since,
                                   boolean expException) {
        print("{}: Since {}", pv, since);
        try {
            verMinSince(pv, since, MSG);
            if (expException)
                fail(AM_NOEX);
        } catch (VersionMismatchException vme) {
            if (expException) {
                print("  " + FMT_EX, vme);
            } else {
                print(vme);
                fail(AM_UNEX);
            }
        }
    }

    @Test
    public void testVerMinSince() {
        print(EOL + "testVerMinSince()");
        verifyVerMinSince(V_1_3, V_1_3, false);
        verifyVerMinSince(V_1_2, V_1_3, true);
        verifyVerMinSince(V_1_1, V_1_3, true);
        verifyVerMinSince(V_1_0, V_1_3, true);

        verifyVerMinSince(V_1_3, V_1_2, false);
        verifyVerMinSince(V_1_2, V_1_2, false);
        verifyVerMinSince(V_1_1, V_1_2, true);
        verifyVerMinSince(V_1_0, V_1_2, true);

        verifyVerMinSince(V_1_3, V_1_1, false);
        verifyVerMinSince(V_1_2, V_1_1, false);
        verifyVerMinSince(V_1_1, V_1_1, false);
        verifyVerMinSince(V_1_0, V_1_1, true);

        verifyVerMinSince(V_1_3, V_1_0, false);
        verifyVerMinSince(V_1_2, V_1_0, false);
        verifyVerMinSince(V_1_1, V_1_0, false);
        verifyVerMinSince(V_1_0, V_1_0, false);

    }

    private void verifySameVer(boolean expException, ProtocolVersion... vers) {
        String arr = Arrays.toString(vers);
        print(arr + "...");
        try {
            sameVersion(arr, vers);
            if (expException)
                fail(AM_NOEX);
            print("no exception");
        } catch (VersionMismatchException vme) {
            if (!expException)
                fail(AM_UNEX);
            print(FMT_EX, vme);
        } catch (Exception e) {
            fail(AM_WREX);
        }
    }

    @Test
    public void testUnmatchedVersions() {
        print(EOL + "testUnmatchedVersions()");
        verifySameVer(false);
        verifySameVer(false, V_1_0);
        verifySameVer(false, V_1_0, V_1_0);
        verifySameVer(true, V_1_0, V_1_1);
        verifySameVer(true, V_1_0, V_1_0, V_1_3);
    }


    private void verifyDeprecated(ProtocolVersion pv, ProtocolVersion depAt,
                                  boolean expException) {
        print("{}: Dep-At {}", pv, depAt);
        try {
            notDeprecated(pv, depAt, MSG);
            if (expException)
                fail(AM_NOEX);
        } catch (VersionMismatchException vme) {
            if (expException) {
                print("  " + FMT_EX, vme);
            } else {
                print(vme);
                fail(AM_UNEX);
            }
        }
    }

    @Test
    public void testNotDeprecated() {
        print(EOL + "testNotDeprecated()");
        verifyDeprecated(V_1_0, V_1_0, true);
        verifyDeprecated(V_1_1, V_1_0, true);
        verifyDeprecated(V_1_2, V_1_0, true);
        verifyDeprecated(V_1_3, V_1_0, true);

        verifyDeprecated(V_1_0, V_1_1, false);
        verifyDeprecated(V_1_1, V_1_1, true);
        verifyDeprecated(V_1_2, V_1_1, true);
        verifyDeprecated(V_1_3, V_1_1, true);

        verifyDeprecated(V_1_0, V_1_2, false);
        verifyDeprecated(V_1_1, V_1_2, false);
        verifyDeprecated(V_1_2, V_1_2, true);
        verifyDeprecated(V_1_3, V_1_2, true);

        verifyDeprecated(V_1_0, V_1_3, false);
        verifyDeprecated(V_1_1, V_1_3, false);
        verifyDeprecated(V_1_2, V_1_3, false);
        verifyDeprecated(V_1_3, V_1_3, true);
    }

}
