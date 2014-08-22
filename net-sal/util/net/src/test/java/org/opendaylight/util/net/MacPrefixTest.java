/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.net;

import org.junit.Test;

import java.util.Arrays;

import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.StringUtils.EOL;
import static org.opendaylight.util.StringUtils.join;
import static org.opendaylight.util.net.MacAddress.MAC_ADDR_SIZE;
import static org.junit.Assert.*;

/**
 * This JUnit test class tests the MacPrefix class.
 *
 * @author Simon Hunt
 */
public class MacPrefixTest {

    private MacPrefix prefix;
    private MacPrefix pCopy;
    private MacPrefix pCopy2;

    private static final int B = 256;
    private static final String COLON = ":";
    private static final String DASH = "-";
    private static final String STAR = "*";

    // prefix arrays
    private static final byte[] PA1 = {0xfe-B};
    private static final byte[] PA2 = {0xfe-B, 0xaa-B};
    private static final byte[] PA3 = {0xfe-B, 0xaa-B, 0x00};
    private static final byte[] PA4 = {0xfe-B, 0xaa-B, 0x00, 0x12};
    private static final byte[] PA5 = {0xfe-B, 0xaa-B, 0x00, 0x12, 0x34};

    private static final byte[][] ARRAYS = { PA1, PA2, PA3, PA4, PA5 };

    // prefix strings with colons
    private static final String PSC1 = "fe";
    private static final String PSC2 = "fe:aa";
    private static final String PSC3 = "fe:aa:00";
    private static final String PSC4 = "fe:aa:00:12";
    private static final String PSC5 = "fe:aa:00:12:34";

    private static final String[] COLONS = { PSC1, PSC2, PSC3, PSC4, PSC5 };

    // prefix strings with dashes
    private static final String PSD1 = "fe";
    private static final String PSD2 = "fe-aa";
    private static final String PSD3 = "fe-aa-00";
    private static final String PSD4 = "fe-aa-00-12";
    private static final String PSD5 = "fe-aa-00-12-34";

    private static final String[] DASHES = { PSD1, PSD2, PSD3, PSD4, PSD5 };

    private static final String E_PREFIX = "Should be a prefix of";
    private static final String E_NOT_PREFIX = "Should NOT be a prefix of";

    // helper method to create the range equivalent to the prefix array
    private MacRange createEquivRange(byte[] bytes) {
        final int size = bytes.length;
        byte[] b = Arrays.copyOf(bytes, MAC_ADDR_SIZE); // padded with zeros
        String macStr = MacAddress.valueOf(b).toString();
        String[] hexBytes = macStr.split(COLON);
        // replace the tail bytes with stars
        for (int i=size; i<MAC_ADDR_SIZE; i++)
            hexBytes[i] = STAR;
        return MacRange.valueOf(join(hexBytes, COLON));
    }

    // helper method to create the range equivalent to the prefix spec
    private MacRange createEquivRange(String spec) {
        String norm = spec.replace(DASH, COLON);
        final int n = norm.split(COLON).length;
        StringBuilder sb = new StringBuilder(norm);
        for (int i=n; i<MAC_ADDR_SIZE; i++)
            sb.append(":*");
        return MacRange.valueOf(sb.toString());
    }

    @Test
    public void basicFromArray() {
        print(EOL + "basicFromArray()");

        for (int i=0,n=ARRAYS.length; i<n; i++) {
            byte[] bytes = ARRAYS[i];
            prefix = MacPrefix.valueOf(bytes);
            MacRange range = createEquivRange(bytes);
            checkPrefix(prefix, bytes.length, range);
        }
    }

    @Test
    public void regExpGood() {
        print(EOL + "regExpGood()");
        for (String s: COLONS) {
            print(s);
            assertTrue("REG-EXP should match", MacPrefix.RE.matcher(s).matches());
        }

        for (String s: DASHES) {
            print(s);
            assertTrue("REG-EXP should match", MacPrefix.RE.matcher(s).matches());
        }
    }

    private static final String[] BAD_SPECS = {
            "",
            "0",
            "bg",
            "ff-",
            "00:",
            "a:b",
            "11:22:33:44:55:66",
            "11:22:33:44:5",
    };

    @Test
    public void regExpBad() {
        print(EOL + "regExpBad()");
        for (String s: BAD_SPECS) {
            print("\"" + s + "\"");
            assertFalse("REG-EXP should NOT match", MacPrefix.RE.matcher(s).matches());
        }
    }

    @Test
    public void basicFromStrings() {
        print(EOL + "basicFromStrings()");

        for (int i=0,n=COLONS.length; i<n; i++) {
            final int expSize = i+1;
            checkPrefixFromSpec(COLONS[i], expSize);
            checkPrefixFromSpec(DASHES[i], expSize);
        }
    }

    private void checkPrefixFromSpec(String spec, int expSize) {
        print("spec> " + spec);
        prefix = MacPrefix.valueOf(spec);
        MacRange range = createEquivRange(spec);
        checkPrefix(prefix, expSize, range);
    }

    private void checkPrefix(MacPrefix p, int expSize, MacRange range) {
        print("  prefix> " + p);
        print("  range>  " + range);
        assertEquals(AM_NEQ, expSize, prefix.size());
        assertTrue(E_PREFIX, prefix.prefixes(range.first()));
        assertTrue(E_PREFIX, prefix.prefixes(range.last()));
        assertTrue(E_PREFIX, prefix.prefixes(range.random()));
    }

    @Test
    public void equality() {
        print(EOL + "equality()");

        MacPrefix mpSize2 = MacPrefix.valueOf("fe:12");
        MacPrefix mpSize3 = MacPrefix.valueOf("fe:12:34");
        verifyNotEqual(mpSize2, mpSize3);

        MacPrefix mp2Copy = MacPrefix.valueOf("FE-12");
        verifyEqual(mpSize2, mp2Copy);

        MacPrefix mp2ArrayCopy = MacPrefix.valueOf(new byte[] {0xfe-B,0x12});
        verifyEqual(mpSize2, mp2ArrayCopy);
    }

    @Test
    public void anExample() {
        print(EOL + "anExample()");

        MacPrefix mpSize2 = MacPrefix.valueOf("fe:12");
        MacPrefix mpSize3 = MacPrefix.valueOf("fe:12:34");

        MacAddress inBoth = MacAddress.valueOf("fe-12-34-12-34-56");
        MacAddress inOne = MacAddress.valueOf("fe-12-35-12-34-56");
        MacAddress inNeither = MacAddress.valueOf("fe-11-34-12-34-56");

        assertTrue(E_PREFIX, mpSize2.prefixes(inBoth));
        assertTrue(E_PREFIX, mpSize3.prefixes(inBoth));

        assertTrue(E_PREFIX, mpSize2.prefixes(inOne));
        assertFalse(E_NOT_PREFIX, mpSize3.prefixes(inOne));

        assertFalse(E_NOT_PREFIX, mpSize3.prefixes(inNeither));
        assertFalse(E_NOT_PREFIX, mpSize3.prefixes(inNeither));
    }

    @Test(expected = NullPointerException.class)
    public void nullByteArray() {
        MacPrefix.valueOf((byte[])null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyByteArray() {
        MacPrefix.valueOf(new byte[] {} );
    }

    @Test(expected = IllegalArgumentException.class)
    public void lengthyByteArray() {
        MacPrefix.valueOf(new byte[] {1, 2, 3, 4, 5, 6} );
    }

    @Test(expected = NullPointerException.class)
    public void nullStringSpec() {
        MacPrefix.valueOf((String)null);
    }

    @Test
    public void badSpecs() {
        print(EOL + "badSpecs()");
        for (String s: BAD_SPECS) {
            print(" >" + s + "<");
            try {
                MacPrefix.valueOf(s);
                fail(AM_NOEX);
            } catch (IllegalArgumentException e) {
                print("EX> " + e);
                assertTrue(AM_WREXMSG, e.getMessage().contains("spec invalid:"));
            }
        }
    }

    @Test
    public void interning() {
        print(EOL + "interning()");
        // string first, then array
        prefix = MacPrefix.valueOf("aa:bb:cc");
        assertEquals(AM_UXS, 3, prefix.size());
        pCopy = MacPrefix.valueOf(new byte[] {0xaa-B, 0xbb-B, 0xcc-B});
        assertSame(AM_NSR, prefix, pCopy);
        verifyEqual(prefix, pCopy);
        print(prefix);

        // array first, then string
        prefix = MacPrefix.valueOf(new byte[] {0x77, 0x88-B, 0x99-B});
        assertEquals(AM_UXS, 3, prefix.size());
        pCopy = MacPrefix.valueOf("77:88:99");
        assertSame(AM_NSR, prefix, pCopy);
        verifyEqual(prefix, pCopy);
        print(prefix);
    }

    @Test
    public void sameReferences() {
        print(EOL + "sameReferences()");
        for (int i=0,n=ARRAYS.length; i<n; i++) {
            print(Arrays.toString(ARRAYS[i]));
            prefix = MacPrefix.valueOf(ARRAYS[i]);
            print(COLONS[i]);
            pCopy = MacPrefix.valueOf(COLONS[i]);
            print(DASHES[i]);
            pCopy2 = MacPrefix.valueOf(DASHES[i]);
            assertSame(prefix, pCopy);
            assertSame(prefix, pCopy2);
            print("  -> " + prefix);
        }
    }

    private static final String[] UNSORTED = {
            "fe",
            "20:11",
            "fe:ab",
            "20",
            "fe:33",
            "ff",
            "70-dd-cc",
    };

    private static final String[] SORTED = {
            "20",
            "20:11",
            "70-dd-cc",
            "fe",
            "fe:33",
            "fe:ab",
            "ff",
    };

    @Test
    public void compareTo() {
        print(EOL + "compareTo()");
        assertEquals(AM_UXS, UNSORTED.length, SORTED.length);

        MacPrefix[] prefixes = new MacPrefix[UNSORTED.length];
        print(EOL + "  unsorted:");
        for (int i=0,n=UNSORTED.length; i<n; i++) {
            prefixes[i] = MacPrefix.valueOf(UNSORTED[i]);
            print("    " + prefixes[i]);
        }
        Arrays.sort(prefixes);

        print(EOL + "  sorted:");
        for (int i=0,n=SORTED.length; i<n; i++) {
            print("    " + prefixes[i]);
            pCopy = MacPrefix.valueOf(SORTED[i]);
            assertSame(AM_NSR, pCopy, prefixes[i]);
        }
    }
}
