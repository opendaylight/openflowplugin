/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.net;

import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.net.Vni.vni;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link VniRange}.
 *
 * @author Simon Hunt
 */
public class VniRangeTest {

    private static final String FMT_EX = "EX> {}";

    private static final int RANDOM_SAMPLE_SIZE = 200;
    private static final int MIN_COUNT = 2;

    private VniRange vra;
    private VniRange vrb;

    @Test
    public void basic() {
        print(EOL + "basic()");
        vra = VniRange.valueOf("0-1");
        print(vra.toDebugString());
        assertEquals(AM_NEQ, vni(0), vra.first());
        assertEquals(AM_NEQ, vni(1), vra.last());
        assertEquals(AM_NEQ, 2, vra.size());
    }

    @Test
    public void basicFromVnis() {
        print(EOL + "basicFromVnis()");
        vra = VniRange.valueOf(vni(0), vni(1));
        print(vra.toDebugString());
        assertEquals(AM_NEQ, vni(0), vra.first());
        assertEquals(AM_NEQ, vni(1), vra.last());
        assertEquals(AM_NEQ, 2, vra.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invertedVnis() {
        VniRange.valueOf(vni(1), vni(0));
    }

    @Test(expected = NullPointerException.class)
    public void nullFirstVni() {
        VniRange.valueOf(null, vni(1));
    }

    @Test(expected = NullPointerException.class)
    public void nullLastVni() {
        VniRange.valueOf(vni(0), null);
    }

    @Test
    public void random() {
        print(EOL + "random()");
        vra = VniRange.valueOf("10-13");
        int[] counts = new int[4];
        for (int i = 0; i<RANDOM_SAMPLE_SIZE; i++) {
            Vni v = vra.random();
            assertTrue("OOR! " + v, v.toInt() >= 10 && v.toInt() <= 13);
            counts[v.toInt()-10]++;
        }
        for (int i=0; i<4; i++) {
            int n = i+10;
            print("{} : N = {}", n, counts[i]);
            assertTrue("Not enough samples for " + n, counts[i] > MIN_COUNT);
        }
    }

    private void checkIter(Iterator<Vni> it, Vni exp) {
        assertEquals("hasNext() incorrect", exp != null, it.hasNext());
        Vni act;
        try {
            act = it.next();
            print("Iter[{}] => {}", it, act);
            if (exp == null)
                fail(AM_NOEX);
            assertEquals(AM_NEQ, exp, act);
        } catch (NoSuchElementException nsee) {
            if (exp != null)
                fail(AM_UNEX);
            print(FMT_EX, nsee);
        }
    }

    @Test
    public void iterate() {
        print(EOL + "iterate()");
        vra = VniRange.valueOf("4-6");
        print(vra);
        assertEquals(AM_UXS, 3, vra.size());
        Iterator<Vni> itOne = vra.iterator();
        Iterator<Vni> itTwo = vra.iterator();

        checkIter(itOne, vni(4));
        checkIter(itOne, vni(5));

        checkIter(itTwo, vni(4));
        checkIter(itTwo, vni(5));

        checkIter(itOne, vni(6));
        checkIter(itOne, null);

        checkIter(itTwo, vni(6));
        checkIter(itTwo, null);

        try {
            itOne.remove();
            fail(AM_NOEX);
        } catch (UnsupportedOperationException e) {
            print(FMT_EX, e);
        }
    }

    @Test
    public void equality() {
        print(EOL + "equality()");
        vra = VniRange.valueOf("16-19");
        vrb = VniRange.valueOf("16-19");
        verifyEqual(vra, vrb);
        assertTrue(AM_HUH, vra.contains(vrb));
        assertTrue(AM_HUH, vrb.contains(vra));
        assertTrue(AM_HUH, vrb.intersects(vra));
        assertTrue(AM_HUH, vra.intersects(vrb));
    }

    @Test(expected = NullPointerException.class)
    public void nullSpec() {
        VniRange.valueOf(null);
    }

    private static final String[] BAD_SPECS = {
            "",
            "-",
            "1-",
            "-1",
            "-1-2",
            "1-2-3",
            "1-2-3-4",
            "2-1",
    };

    @Test
    public void badSpecs() {
        for (String s: BAD_SPECS) {
            try {
                print("bad spec: '{}'", s);
                VniRange.valueOf(s);
                fail(AM_NOEX);
            } catch (IllegalArgumentException e) {
                print(FMT_EX, e);
            }
        }
    }

    @Test
    public void singleItem() {
        print(EOL + "singleItem()");
        vra = VniRange.valueOf("13-13");
        assertEquals(AM_NEQ, vni(13), vra.first());
        assertEquals(AM_NEQ, vni(13), vra.last());
        assertEquals(AM_NEQ, 1, vra.size());
        Iterator<Vni> it = vra.iterator();
        assertTrue(AM_HUH, it.hasNext());
        Vni v = it.next();
        assertEquals(AM_NEQ, vni(13), v);
        assertFalse(AM_HUH, it.hasNext());
    }

    @Test
    public void contains() {
        print(EOL + "contains()");
        vra = VniRange.valueOf("203-209");
        assertFalse(AM_HUH, vra.contains(vni(202)));
        assertTrue(AM_HUH, vra.contains(vni(203)));
        assertTrue(AM_HUH, vra.contains(vni(207)));
        assertFalse(AM_HUH, vra.contains(vni(210)));
    }

    @Test
    public void subrange() {
        print(EOL + "subrange()");
        vra = VniRange.valueOf("8-12");
        assertTrue(AM_HUH, vra.contains(VniRange.valueOf("8-9")));
        assertTrue(AM_HUH, vra.contains(VniRange.valueOf("10-10")));
        assertTrue(AM_HUH, vra.contains(VniRange.valueOf("9-12")));
        assertFalse(AM_HUH, vra.contains(VniRange.valueOf("9-13")));
        assertFalse(AM_HUH, vra.contains(VniRange.valueOf("6-11")));
        assertFalse(AM_HUH, vra.contains(VniRange.valueOf("4-6")));
        assertFalse(AM_HUH, vra.contains(VniRange.valueOf("13-123")));
    }
    @Test
    public void intersects() {
        print(EOL + "intersects()");
        vra = VniRange.valueOf("8-12");
        assertTrue(AM_HUH, vra.intersects(VniRange.valueOf("8-9")));
        assertTrue(AM_HUH, vra.intersects(VniRange.valueOf("10-10")));
        assertTrue(AM_HUH, vra.intersects(VniRange.valueOf("9-12")));
        assertTrue(AM_HUH, vra.intersects(VniRange.valueOf("9-13")));
        assertTrue(AM_HUH, vra.intersects(VniRange.valueOf("6-11")));
        assertFalse(AM_HUH, vra.intersects(VniRange.valueOf("4-6")));
        assertFalse(AM_HUH, vra.intersects(VniRange.valueOf("13-123")));
    }

    @Test
    public void worksWithHexToo() {
        print(EOL + "worksWithHexToo()");
        vra = VniRange.valueOf("0x20-0x2f");
        print(vra.toDebugString());
        assertEquals(AM_NEQ, vni(32), vra.first());
        assertEquals(AM_NEQ, vni(47), vra.last());
        assertEquals(AM_NEQ, 16, vra.size());
    }

    @Test
    public void biggestPossibleRange() {
        StringBuilder sb = new StringBuilder();
        sb.append(Vni.MIN_VALUE).append("-").append(Vni.MAX_VALUE);
        vra = VniRange.valueOf(sb.toString());
        print(vra.toDebugString());
        assertEquals(AM_NEQ, vni(Vni.MIN_VALUE), vra.first());
        assertEquals(AM_NEQ, vni(Vni.MAX_VALUE), vra.last());
        assertEquals(AM_NEQ, Vni.MAX_VALUE + 1, vra.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void numberTooBig() {
        VniRange.valueOf("0-0x1000000");
    }
}
