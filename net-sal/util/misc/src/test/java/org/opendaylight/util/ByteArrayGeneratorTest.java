/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util;

import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.ByteUtils.byteToInt;
import static org.junit.Assert.*;

/**
 * This JUnit test class tests the ByteArrayGenerator class.
 *
 * @author Simon Hunt
 */
public class ByteArrayGeneratorTest {

    private ByteArrayGenerator bag;

    @Test(expected = NullPointerException.class)
    public void nullSpec() {
        ByteArrayGenerator.create(null);
    }

    @Test(expected = NullPointerException.class)
    public void nullSpecHex() {
        ByteArrayGenerator.createFromHex(null);
    }

    private static final String[] BAD_SPECS = {
            "",
            "-1",
            "256",
            "1-300",
            "1:2:345",
            "1-2:3-4:-1",
            "255:",
            "0:255-30",
            "10-2",
            "5-5",
    };

    private static final String[] BAD_SPECS_HEX = {
            "",
            "-1",
            "100",
            "1-300",
            "1:2:345",
            "1-2:3-4:-1",
            "aa:",
            "00:ff-fa",
            "a-2",
            "5-5",
            "000",
    };

    private static final String[] GOOD_SPECS = {
            "0",
            "1",
            "5",
            "15",
            "30",
            "99",
            "199",
            "120",
            "240",
            "249",
            "251",
            "255",
            "0-10",
            "5-255:0:45-50",
            "3:4:005",
            "*",
            "15:5:2:*",
    };

    private static final String[] GOOD_SPECS_HEX = {
            "0",
            "1",
            "5",
            "f",
            "F",
            "1e",
            "1E",
            "63",
            "c7",
            "C7",
            "78",
            "f0",
            "F0",
            "fb",
            "FB",
            "ff",
            "FF",
            "0-a",
            "0-A",
            "5-ff:0:2d-32",
            "5-FF:0:2D-32",
            "3:4:05",
            "*",
            "*:4:*",
            "ff:*",
    };

    @Test
    public void badSpecs() {
        print(EOL + "badSpecs()");
        for (String spec: BAD_SPECS) {
            print("  [" + spec + "]");
            try {
                ByteArrayGenerator.create(spec);
                fail(AM_NOEX);
            } catch (IllegalArgumentException e) {
                print(e.getMessage());
                assertTrue(AM_HUH, e.getMessage().contains(ByteArrayGenerator.E_MAL));
            }
        }
    }

    @Test
    public void badSpecsHex() {
        print(EOL + "badSpecsHex()");
        for (String spec: BAD_SPECS_HEX) {
            print("  [" + spec + "]");
            try {
                ByteArrayGenerator.createFromHex(spec);
                fail(AM_NOEX);
            } catch (IllegalArgumentException e) {
                print(e.getMessage());
                assertTrue(AM_HUH, e.getMessage().contains(ByteArrayGenerator.E_MAL));
            }
        }
    }

    @Test
    public void goodSpecs() {
        print (EOL + "goodSpecs()");
        for (String spec: GOOD_SPECS) {
            print ("  [" + spec + "]");
            bag = ByteArrayGenerator.create(spec);
            print(bag.toDebugString());
            assertEquals(AM_NEQ, spec, bag.getSpec());
        }
    }

    @Test
    public void goodSpecsHex() {
        print (EOL + "goodSpecsHex()");
        for (String spec: GOOD_SPECS_HEX) {
            print ("  [" + spec + "]");
            bag = ByteArrayGenerator.createFromHex(spec);
            print(bag.toDebugString());
            assertEquals(AM_NEQ, spec, bag.getSpec());
        }
    }

    private void validateSpec(ByteArrayGenerator bag, String norm, int size, int rss) {
        print("  validating " + bag.getSpec() + " ...");
        print(bag.toDebugString());
        assertEquals(AM_NEQ, norm, bag.getNormalizedSpec());
        assertEquals(AM_UXS, size, bag.size());
        assertEquals(AM_UXS, BigInteger.valueOf(rss), bag.resultSpaceSize());
    }

    private void validateDecSpec(String spec, String norm, int size, int rss) {
        bag = ByteArrayGenerator.create(spec);
        validateSpec(bag, norm, size, rss);
    }

    @Test
    public void genSize() {
        print(EOL + "genSize()");

        validateDecSpec("1", "01", 1, 1);
        validateDecSpec("230-240", "e6-f0", 1, 11);
        validateDecSpec("1:2", "01:02", 2, 1);
        validateDecSpec("230-240:240-242", "e6-f0:f0-f2", 2, 33);
        validateDecSpec("1:2:255", "01:02:ff", 3, 1);
        validateDecSpec("230-240:240-242:0", "e6-f0:f0-f2:00", 3, 33);
        validateDecSpec("240-242:0-255", "f0-f2:*", 2, 3 * 256);
    }

    private void validateHexSpec(String spec, String norm, int size, int rss) {
        bag = ByteArrayGenerator.createFromHex(spec);
        validateSpec(bag, norm, size, rss);
    }

    @Test
    public void genSizeHex() {
        print(EOL + "genSizeHex()");
        validateHexSpec("1", "01", 1, 1);
        validateHexSpec("a-b", "0a-0b", 1, 2);
        validateHexSpec("1:2", "01:02", 2, 1);
        validateHexSpec("F0-FE:35-3B", "f0-fe:35-3b", 2, 15 * 7);
        validateHexSpec("1:2:ff", "01:02:ff", 3, 1);
        validateHexSpec("ab-af:c0-c4:0", "ab-af:c0-c4:00", 3, 5 * 5);
        validateHexSpec("0-ff:0-ff", "*:*", 2, 256 * 256);
    }

    @Test
    public void singleByte() {
        print(EOL + "singleByte()");
        final String spec = "255";
        bag = ByteArrayGenerator.create(spec);
        byte[] array = bag.generate();
        print(Arrays.toString(array));
        print(bag.toDebugString());
        assertEquals(AM_UXS, 1, array.length);
        assertEquals(AM_NEQ, (byte)255, array[0]);
        assertEquals(AM_NEQ, "ff", bag.getNormalizedSpec());
    }

    @Test
    public void singleByteHex() {
        print(EOL + "singleByteHex()");
        final String spec = "FF";
        bag = ByteArrayGenerator.createFromHex(spec);
        byte[] array = bag.generate();
        print(Arrays.toString(array));
        assertEquals(AM_UXS, 1, array.length);
        assertEquals(AM_NEQ, (byte)255, array[0]);
        assertEquals(AM_NEQ, "ff", bag.getNormalizedSpec());
    }

    private static final String[] BOUNDS_SPECS = {
            "0-8",
            "21-25",
            "126-131",
            "200-209",
            "254-255",
    };

    private static final String[] BOUNDS_SPECS_HEX = {
            "0-d",
            "15-1a",
            "7e-83",
            "c8-d1",
            "fe-ff",
    };

    @Test
    public void singleByteRange() {
        print(EOL + "singleByteRange()");
        for (String spec: BOUNDS_SPECS) {
            print (EOL + "for this spec [" + spec + "]");
            int lo = getLo(spec);
            int hi = getHi(spec);
            print ("  lo=" + lo + ", hi=" + hi);

            bag = ByteArrayGenerator.create(spec);
            print(bag.toDebugString());
            // uh, lets try this a few times
            final int count = 200;
            for (int i=0; i<count; i++) {
                byte[] array = bag.generate();
                if (i<10) {
                    print(Arrays.toString(array) + "  " + byteToInt(array[0]));
                } else if (i==10) {
                    print("...");
                }

                assertTrue(AM_OOB, withinBounds(lo, hi, array[0]));
            }
        }
    }

    @Test
    public void singleByteRangeHex() {
        print(EOL + "singleByteRangeHex()");
        for (String spec: BOUNDS_SPECS_HEX) {
            print (EOL + "for this spec [" + spec + "]");
            int lo = getLoHex(spec);
            int hi = getHiHex(spec);
            print ("  lo=" + lo + ", hi=" + hi);

            bag = ByteArrayGenerator.createFromHex(spec);
            print(bag.toDebugString());
            // uh, lets try this a few times
            final int count = 200;
            for (int i=0; i<count; i++) {
                byte[] array = bag.generate();
                if (i<10) {
                    print(Arrays.toString(array) + "  " + byteToInt(array[0]));
                } else if (i==10) {
                    print("...");
                }

                assertTrue(AM_OOB, withinBounds(lo, hi, array[0]));
            }
        }
    }

    private int getLoHex(String spec) {
        String[] pieces = spec.split("-");
        return Integer.parseInt(pieces[0], 16);
    }

    private int getHiHex(String spec) {
        String[] pieces = spec.split("-");
        return Integer.parseInt(pieces[1], 16);
    }

    private int getLo(String spec) {
        String[] pieces = spec.split("-");
        return Integer.valueOf(pieces[0]);
    }

    private int getHi(String spec) {
        String[] pieces = spec.split("-");
        return Integer.valueOf(pieces[1]);
    }

    // predicate that returns true if byte b is within the lo/hi bounds
    private boolean withinBounds(int lo, int hi, byte b) {
        int byt = ByteUtils.byteToInt(b);
        return byt >= lo && byt <= hi;
    }

    private static final String SPEC_1 = "35:127-255:0-255:44";
    private static final String SPEC_2 = "23:7F-FF:*:2C";
    private static final String SPEC_3 = "23:7f-ff:00-ff:2c";
    private static final String SPEC_X = "23:7f-ff:*:2c:0-8";
    private static final String SPEC_Y = "23:7e-ff:*:2c";
    private static final String SPEC_Z = "23:7e-ff";

    @Test
    public void containsArrays() {
        print(EOL + "containsArrays()");
        bag = ByteArrayGenerator.create(SPEC_1);
        print(bag.toDebugString());
        assertTrue(AM_HUH, bag.contains(new byte[] {35, 127, 2, 44}));
        assertTrue(AM_HUH, bag.contains(new byte[] {35, -128, 2, 44}));
        assertTrue(AM_HUH, bag.contains(new byte[] {35, -1, 2, 44}));
        assertFalse(AM_HUH, bag.contains(new byte[] {32, 127, 2, 44}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void containsMismatch() {
        bag = ByteArrayGenerator.create(SPEC_1);
        bag.contains(new byte[] {1, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void isSupersetMismatch() {
        bag = ByteArrayGenerator.create(SPEC_1);
        bag.isSuperset(ByteArrayGenerator.createFromHex(SPEC_Z));
    }

    @Test(expected = IllegalArgumentException.class)
    public void intersectsMismatch() {
        bag = ByteArrayGenerator.create(SPEC_1);
        bag.intersects(ByteArrayGenerator.createFromHex(SPEC_Z));
    }


    @Test(expected = NullPointerException.class)
    public void containsNull() {
        bag = ByteArrayGenerator.create(SPEC_1);
        bag.contains(null);
    }

    @Test(expected = NullPointerException.class)
    public void isSupersetNull() {
        bag = ByteArrayGenerator.create(SPEC_1);
        bag.isSuperset(null);
    }

    @Test(expected = NullPointerException.class)
    public void intersectsNull() {
        bag = ByteArrayGenerator.create(SPEC_1);
        bag.intersects(null);
    }

    @Test
    public void generatorEqualsHashCode() {
        print(EOL + "generatorEqualsHashCode()");
        ByteArrayGenerator bag1 = ByteArrayGenerator.create(SPEC_1);
        ByteArrayGenerator bag2 = ByteArrayGenerator.createFromHex(SPEC_2);
        ByteArrayGenerator bag3 = ByteArrayGenerator.createFromHex(SPEC_3);
        print(bag1.toDebugString());
        print(bag2.toDebugString());
        print(bag3.toDebugString());
        verifyEqual(bag1, bag2);
        verifyEqual(bag1, bag3);
        verifyEqual(bag2, bag3);

        ByteArrayGenerator bagX = ByteArrayGenerator.createFromHex(SPEC_X);
        print(bagX.toDebugString());
        verifyNotEqual(bagX, bag1);

        ByteArrayGenerator bagY = ByteArrayGenerator.createFromHex(SPEC_Y);
        print(bagY.toDebugString());
        verifyNotEqual(bagY, bag1);
    }

    private static final String SPEC_OUTER = "50-a0:*";
    private static final String SPEC_MIDDLE = "54-9f:44-c3";
    private static final String SPEC_INNER = "6a-7a:4a-ba";
    private static final String SPEC_SANCTUM = "70:90-a0";
    private static final String SPEC_STRADDLES = "60-b0:35-46";
    private static final String SPEC_OUTSIDE = "00-30:*";
    
    @Test
    public void supersets() {
        print(EOL + "supersets()");
        ByteArrayGenerator bagOuter = ByteArrayGenerator.createFromHex(SPEC_OUTER);
        ByteArrayGenerator bagMiddle = ByteArrayGenerator.createFromHex(SPEC_MIDDLE);
        ByteArrayGenerator bagInner = ByteArrayGenerator.createFromHex(SPEC_INNER);
        ByteArrayGenerator bagSanctum = ByteArrayGenerator.createFromHex(SPEC_SANCTUM);
        ByteArrayGenerator bagStraddles = ByteArrayGenerator.createFromHex(SPEC_STRADDLES);
        ByteArrayGenerator bagOutside = ByteArrayGenerator.createFromHex(SPEC_OUTSIDE);
        print("  OUTER: " + bagOuter.toDebugString());
        print("  MIDDLE: " + bagMiddle.toDebugString());
        print("  INNER: " + bagInner.toDebugString());
        print("  SANCTUM: " + bagSanctum.toDebugString());
        print("  STRADDLES: " + bagStraddles.toDebugString());
        print("  OUTSIDE: " + bagOutside.toDebugString());


        checkSupersetRelationship(bagOuter, bagMiddle);
        checkSupersetRelationship(bagOuter, bagInner);
        checkSupersetRelationship(bagOuter, bagSanctum);
        checkSupersetRelationship(bagMiddle, bagInner);
        checkSupersetRelationship(bagMiddle, bagSanctum);
        checkSupersetRelationship(bagInner, bagSanctum);

        assertFalse(SSWNE, bagOuter.isSuperset(bagStraddles));
        assertFalse(SSWNE, bagStraddles.isSuperset(bagOuter));
        assertFalse(SSWNE, bagOutside.isSuperset(bagOuter));

        assertTrue(NI, bagStraddles.intersects(bagOuter));
        assertTrue(NI, bagStraddles.intersects(bagMiddle));
        assertFalse(IWNE, bagStraddles.intersects(bagInner));
        assertFalse(IWNE, bagStraddles.intersects(bagSanctum));

        assertFalse(IWNE, bagOutside.intersects(bagOuter));
        assertFalse(IWNE, bagOutside.intersects(bagMiddle));
        assertFalse(IWNE, bagOutside.intersects(bagInner));
        assertFalse(IWNE, bagOutside.intersects(bagSanctum));

    }

    private static final String NASS = "not a superset";
    private static final String SSWNE = "superset when not expected";
    private static final String NI = "no intersection";
    private static final String IWNE = "intersection when not expected";

    private void checkSupersetRelationship(ByteArrayGenerator superset, ByteArrayGenerator subset) {
        assertTrue(NASS, superset.isSuperset(superset)); // a superset of itself
        assertTrue(NASS, subset.isSuperset(subset)); // a superset of itself
        assertTrue(NASS, superset.isSuperset(subset));
        assertFalse(SSWNE, subset.isSuperset(superset));
        assertTrue(NI, superset.intersects(superset)); // intersects itself
        assertTrue(NI, subset.intersects(subset)); // intersects itself
        assertTrue(NI, superset.intersects(subset)); // superset intersects a subset
        assertTrue(NI, subset.intersects(superset)); // subset intersects a superset
    }

    // === ByteArrayIterator ===========================================================

    @Test
    public void simpleIterator() {
        print(EOL + "simpleIterator()");
        List<byte[]> returnedArrays = new ArrayList<byte[]>();
        bag = ByteArrayGenerator.create("2:0-5");
        for (Iterator<byte[]> it = bag.iterator(); it.hasNext(); ) {
            byte[] array = it.next();
            print(Arrays.toString(array));
            returnedArrays.add(array);

            // emergency break
            if (returnedArrays.size() > 20) break;
        }
        assertEquals(AM_UXS, 6, returnedArrays.size());
    }

    @Test
    public void iteratorReturnsNullOnceDone() {
        print(EOL + "iteratorReturnsNullOnceDone()");
        bag = ByteArrayGenerator.create("1-2:3-5");
        Iterator<byte[]> iter = bag.iterator();
        checkIter(iter, 1, 3);
        checkIter(iter, 1, 4);
        checkIter(iter, 1, 5);
        checkIter(iter, 2, 3);
        checkIter(iter, 2, 4);
        checkIter(iter, 2, 5);
        checkIter(iter, 0, 0);
        checkIter(iter, 0, 0);
        checkIter(iter, 0, 0);
    }

    private void checkIter(Iterator<byte[]> iter, int hi, int lo) {
        if (hi > 0) {
            // we are expecting a value out of the iterator
            assertTrue("hasNext() should be true", iter.hasNext());
            byte[] ba = iter.next();

            if (ba == null)
                print(" ITER> null");
            else
                print(" ITER> " + ba[0] + ", " + ba[1]);

            assert ba != null;
            assertEquals("unexpected value", hi, ba[0]);
            assertEquals("unexpected value", lo, ba[1]);
        } else {
            // we are not expecting anything more out of the iterator
            assertFalse("hasNext() should be false", iter.hasNext());
            byte[] ba = iter.next();
            assertNull("not null", ba);
            print(" ITER> null");
        }
    }


    @Test
    public void ipRangeIterator() {
        print (EOL + "ipRangeIterator()");
        List<byte[]> returnedArrays = new ArrayList<byte[]>();
        bag = ByteArrayGenerator.create("15:29:37:*");
        for (Iterator<byte[]> it = bag.iterator(); it.hasNext(); ) {
            byte[] array = it.next();
            print(Arrays.toString(array));
            returnedArrays.add(array);
        }
        assertEquals(AM_UXS, 256, returnedArrays.size());
    }


    @Test
    public void multiByteRanges() {
        print (EOL + "multiByteRanges()");
        List<byte[]> returnedArrays = new ArrayList<byte[]>();
        bag = ByteArrayGenerator.createFromHex("20-22:0-1");
        for (Iterator<byte[]> it = bag.iterator(); it.hasNext(); ) {
            byte[] array = it.next();
            print(Arrays.toString(array));
            returnedArrays.add(array);
        }
        assertEquals(AM_UXS, 6, returnedArrays.size());
    }

}
