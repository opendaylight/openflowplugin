/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.cache;

import org.opendaylight.util.TimeUtils;
import org.opendaylight.util.test.FakeTimeUtils;
import org.junit.Before;
import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.cache.AbstractAgeOutTest.Hobbit.*;
import static org.opendaylight.util.cache.AgeOutHashMap.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * Unit tests for AgeOutHashMap.
 *
 * @author Simon Hunt
 */
public class AgeOutHashMapTest extends AbstractAgeOutTest {

    private class TestAgeOutHashMap extends AgeOutHashMap<Hobbit, String> {
        public TestAgeOutHashMap(long testAgeOut) {
            super(testAgeOut);
        }
        public TestAgeOutHashMap() {
            super();
        }

        @Override
        TimeUtils time() {
            return fake.timeUtils();
        }
    }

    FakeTimeUtils fake;
    AgeOutHashMap<Hobbit, String> map;

    private void advance(int ms) {
        fake.advanceMs(ms);
        print("{} {}", map.hmsn(map.now()), map);
    }

    @Before
    public void setUp() {
        fake = FakeTimeUtils.getInstance(FakeTimeUtils.Advance.MANUAL);
        map = new TestAgeOutHashMap(TEST_AGE_OUT);
        assertEquals(AM_NEQ, 0, map.size());
    }

    @Test
    public void basic() {
        startTest("basic");
        print(map);
        assertEquals(AM_NEQ, 0, map.prune());
        assertEquals(AM_NEQ, "{age=100ms,map={}}", map.toString());
        String tstr = map.hmsn(map.now());
        print("Current Time: {}", tstr);
        assertEquals(AM_NEQ, "00:00:00.000", tstr);
        endTest();
    }

    @Test
    public void defaultAgeOut() {
        startTest("defaultAgeOut");
        map = new TestAgeOutHashMap();
        print(map);
        assertEquals(AM_NEQ, DEFAULT_AGE_OUT_MS, map.getAgeOutMs());
        endTest();
    }

    @Test
    public void changeAgeOut() {
        startTest("changeAgeOut");
        print(map);
        assertEquals(AM_NEQ, TEST_AGE_OUT, map.getAgeOutMs());
        map.setAgeOut(25);
        print(map);
        assertEquals(AM_NEQ, 25, map.getAgeOutMs());
        endTest();
    }

    @Test
    public void createAgeOutTooSmall() {
        startTest("createAgeOutTooSmall");
        long oneLess = MIN_AGE_OUT_MS - 1;
        try {
            print("creating map with age-out of {}...", oneLess);
            map = new TestAgeOutHashMap(oneLess);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(FMT_EX, e);
            assertEquals(AM_NEQ, E_AGEOUT_TOO_SMALL + oneLess, e.getMessage());
        } catch (Exception e) {
            fail(AM_WREX);
        }
        endTest();
    }

    @Test
    public void setAgeOutTooSmall() {
        startTest("setAgeOutTooSmall");
        long oneLess = MIN_AGE_OUT_MS - 1;
        try {
            print("setting map age-out to {}...", oneLess);
            map.setAgeOut(oneLess);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(FMT_EX, e);
            assertEquals(AM_NEQ, E_AGEOUT_TOO_SMALL + oneLess, e.getMessage());
        } catch (Exception e) {
            fail(AM_WREX);
        }
        endTest();
    }


    @Test
    public void basicPutGetRemove() {
        startTest("basicPutGetRemove");
        print(map);
        map.put(FRODO, RING);
        print(map);
        assertEquals(AM_UXS, 1, map.prune());
        String val = map.get(FRODO);
        assertEquals(AM_NEQ, RING, val);
        val = map.put(FRODO, SWORD);
        print(map);
        assertEquals(AM_UXS, 1, map.prune());
        assertEquals(AM_NEQ, RING, val);
        val = map.get(FRODO);
        assertEquals(AM_NEQ, SWORD, val);
        assertEquals(AM_UXS, 1, map.prune());
        val = map.remove(FRODO);
        assertEquals(AM_NEQ, SWORD, val);
        assertEquals(AM_HUH, null, map.get(FRODO));
        assertEquals(AM_UXS, 0, map.prune());
        endTest();
    }

    @Test
    public void putOverInvalidated() {
        startTest("putOverInvalidated");
        map.put(FRODO, RING);
        advance(120);
        String result = map.put(FRODO, SWORD);
        assertEquals(AM_NEQ, null, result);
        assertEquals(AM_NEQ, SWORD, map.get(FRODO));
        assertEquals(AM_UXS, 1, map.prune());
        endTest();
    }

    @Test
    public void basicAgeout() {
        startTest("basicAgeout");
        map.put(SAM, BREAD);
        advance(60);
        map.put(MERRY, STAFF);
        advance(30);
        assertEquals(AM_NEQ, BREAD, map.get(SAM));
        assertEquals(AM_NEQ, STAFF, map.get(MERRY));
        assertEquals(AM_UXS, 2, map.prune());
        advance(60);
        assertEquals(AM_HUH, null, map.get(SAM)); // silent remove SAM
        assertEquals(AM_NEQ, STAFF, map.get(MERRY));
        assertEquals(AM_UXS, 1, map.prune());
        advance(60);
        assertEquals(AM_HUH, null, map.get(SAM));
        assertEquals(AM_HUH, null, map.get(MERRY)); // silent remove MERRY
        assertEquals(AM_UXS, 0, map.prune());
        endTest();
    }

    @Test
    public void touch() {
        startTest("touch");
        map.put(FRODO, RING);
        advance(60);
        map.touch(FRODO);
        advance(60);
        assertEquals(AM_NEQ, RING, map.get(FRODO));
        assertEquals(AM_UXS, 1, map.prune());
        advance(100);
        map.touch(FRODO); // silent remove FRODO
        assertEquals(AM_HUH, null, map.get(FRODO));
        assertEquals(AM_UXS, 0, map.prune());
        endTest();
    }

    @Test
    public void touchOrPut() {
        startTest("touchOrPut");
        assertEquals(AM_HUH, null, map.get(PIPPIN));
        advance(50);
        map.touchOrPut(PIPPIN, STAFF);
        advance(50);
        assertEquals(AM_NEQ, STAFF, map.get(PIPPIN));
        map.touchOrPut(PIPPIN, SWORD);
        advance(60);
        // expect STAFF, not SWORD, because entry existed at the time
        assertEquals(AM_NEQ, STAFF, map.get(PIPPIN));
        advance(100);
        map.touchOrPut(PIPPIN, SWORD);
        advance(10);
        // expect SWORD, because entry had expired, so given value was used
        assertEquals(AM_NEQ, SWORD, map.get(PIPPIN));
        endTest();
    }

    @Test
    public void sizeAndPrune() {
        startTest("sizeAndPrune");
        map.put(PIPPIN, STAFF);
        map.put(MERRY, SWORD);
        map.put(SAM, BREAD);
        advance(40);
        map.put(FRODO, RING);
        assertEquals(AM_UXS, 4, map.prune());
        advance(70);
        assertEquals(AM_UXS, 1, map.prune());
        advance(40);
        assertEquals(AM_UXS, 0, map.prune());
        endTest();
    }

    @Test
    public void getDoesntBumpTime() {
        startTest("getDoesntBumpTime");
        map.put(FRODO, RING);
        advance(40);
        assertEquals(AM_NEQ, RING, map.get(FRODO));
        advance(40);
        assertEquals(AM_NEQ, RING, map.get(FRODO));
        advance(40);
        assertEquals(AM_HUH, null, map.get(FRODO)); // silent remove FRODO
        endTest();
    }

    @Test
    public void setAgeOut() {
        startTest("setAgeOut");
        map.put(FRODO, RING);
        advance(30);
        map.put(SAM, BREAD);
        advance(30);
        map.put(MERRY, SWORD);
        advance(30);
        assertEquals(AM_NEQ, RING, map.get(FRODO));
        assertEquals(AM_NEQ, BREAD, map.get(SAM));
        assertEquals(AM_NEQ, SWORD, map.get(MERRY));
        assertEquals(AM_UXS, 3, map.prune());
        map.setAgeOut(140); // calls prune() - no op
        assertEquals(AM_UXS, 3, map.prune());
        map.setAgeOut(40); // calls prune() - removes FRODO, SAM
        assertEquals(AM_UXS, 1, map.prune());
        assertEquals(AM_NEQ, null, map.get(FRODO));
        assertEquals(AM_NEQ, null, map.get(SAM));
        assertEquals(AM_NEQ, SWORD, map.get(MERRY));
        advance(10);
        assertEquals(AM_HUH, null, map.get(MERRY)); // silent remove MERRY
        assertEquals(AM_UXS, 0, map.prune());
        endTest();
    }

}
