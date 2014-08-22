/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.cache;

import static org.opendaylight.util.junit.TestTools.AM_HUH;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.AM_UXS;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.cache.AbstractAgeOutTest.Hobbit.FRODO;
import static org.opendaylight.util.cache.AbstractAgeOutTest.Hobbit.MERRY;
import static org.opendaylight.util.cache.AbstractAgeOutTest.Hobbit.PIPPIN;
import static org.opendaylight.util.cache.AbstractAgeOutTest.Hobbit.SAM;
import static org.opendaylight.util.cache.AgeOutHashMap.DEFAULT_AGE_OUT_MS;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.opendaylight.util.TimeUtils;
import org.opendaylight.util.test.FakeTimeUtils;
import org.opendaylight.util.test.FakeTimeUtils.Advance;

/**
 * Unit tests for {@link org.opendaylight.util.cache.NotedAgeOutHashMap}.
 *
 * @author Simon Hunt
 */
public class NotedAgeOutHashMapTest extends AbstractAgeOutTest {

    private class TestMap extends NotedAgeOutHashMap<String, Hobbit> {
        public TestMap(long testAgeOut, boolean testAgeOutDeadwoodOnly) {
            super(testAgeOut, testAgeOutDeadwoodOnly);
        }
        @Override
        TimeUtils time() {
            return fake.timeUtils();
        }
    }

    FakeTimeUtils fake;
    NotedAgeOutHashMap<String, Hobbit> map;

    private void advance(int ms) {
        fake.advanceMs(ms);
        print("{} {}", map.hmsn(map.now()), map);
    }

    private void verifyDeadwood(Hobbit... expected) {
        Set<Hobbit> d = map.clearDeadwood();
        for (Hobbit h: expected)
            assertTrue("Missing hobbit: " + h, d.contains(h));
        assertEquals("Wrong deadwood size", expected.length, d.size());
    }

    @Before
    public void setUp() {
        fake = FakeTimeUtils.getInstance(Advance.MANUAL);
        map = new TestMap(TEST_AGE_OUT, false);
        assertEquals(AM_NEQ, 0, map.size());
    }
    
    @Test
    public void basic() {
        startTest("basic");
        map.put(RING, SAM);
        advance(1);
        assertEquals(AM_UXS, 1, map.size());
        endTest();
    }

    @Test
    public void defaultConstructor() {
        startTest("defaultConstructor");
        map = new NotedAgeOutHashMap<String, Hobbit>();
        assertEquals(AM_NEQ, DEFAULT_AGE_OUT_MS, map.getAgeOutMs());
        endTest();
    }

    @Test
    public void prune() {
        startTest("prune");
        map.put(RING, SAM);
        advance(30);
        map.put(SWORD, FRODO);
        advance(30);
        map.put(STAFF, PIPPIN);
        advance(30);
        // no deadwood, as all are still active
        verifyDeadwood();
        advance(50);
        // no deadwood, as invalid SAM,FRODO haven't been noticed yet
        verifyDeadwood();
        // all entries still there; we haven't removed the aged-out entries
        assertEquals(AM_UXS, 3, map.size());
        // but when we prune, they're moved to the woodpile
        assertEquals(AM_UXS, 1, map.prune());
        // deadwood after pruning
        verifyDeadwood(FRODO, SAM);
        // clearing the deadwood is a destructive read
        verifyDeadwood();
        advance(20);
        assertEquals(AM_UXS, 0, map.prune());
        verifyDeadwood(PIPPIN);
        verifyDeadwood();
        advance(1);
        endTest();
    }

    @Test
    public void silentRemoveFromSetAgeOut() {
        startTest("silentRemoveFromSetAgeOut");
        map.put(BREAD, MERRY);
        advance(60);
        assertEquals(AM_NEQ, MERRY, map.get(BREAD));
        assertEquals(AM_UXS, 1, map.prune());
        verifyDeadwood();

        map.setAgeOut(40);
        verifyDeadwood(MERRY);
        assertEquals(AM_NEQ, null, map.get(BREAD));
        assertEquals(AM_UXS, 0, map.size());
        advance(1);
        endTest();
    }

    @Test
    public void silentRemoveFromTouch() {
        startTest("silentRemoveFromTouch");
        map.put(STAFF, SAM);
        advance(120);
        // haven't noticed that SAM has aged-out yet
        verifyDeadwood();
        // since SAM aged-out, the result from touch() is false
        assertFalse(AM_HUH, map.touch(STAFF));
        // in the process, SAM was moved to the woodpile
        verifyDeadwood(SAM);
        assertEquals(AM_UXS, 0, map.size());
        advance(1);
        endTest();
    }

    @Test
    public void noSilentRemoveFromTouchOrPut() {
        startTest("noSilentRemoveFromTouchOrPut");
        map.put(STAFF, SAM);
        advance(120);
        // haven't noticed that SAM has aged-out yet
        verifyDeadwood();
        // since SAM aged-out, the result from touchOrPut() is false
        //  given that we are adding a new entry back in
        assertFalse(AM_HUH, map.touchOrPut(STAFF, SAM));
        // but the "old" SAM was NOT moved to the woodpile
        verifyDeadwood();
        // The theory being that, since we only care about SAM's presence in
        // the map, the fact that he previously aged-out (and we didn't notice)
        // is moot.
        assertEquals(AM_UXS, 1, map.size());
        advance(1);
        endTest();
    }

    @Test
    public void silentRemoveFromGet() {
        startTest("silentRemoveFromGet");
        map.put(BREAD, MERRY);
        advance(60);
        // still valid
        assertEquals(AM_NEQ, MERRY, map.get(BREAD));
        advance(60);
        // haven't noticed that MERRY has aged-out yet
        verifyDeadwood();
        // trying to get MERRY will return null
        assertEquals(AM_NEQ, null, map.get(BREAD));
        // and will result in MERRY being wood-piled
        verifyDeadwood(MERRY);
        assertEquals(AM_UXS, 0, map.size());
        advance(1);
        endTest();
    }

    @Test
    public void silentRemoveFromRemove() {
        startTest("silentRemoveFromRemove");
        map.put(RING, FRODO);
        advance(60);
        map.put(STAFF, SAM);
        advance(60);
        // FRODO aged-out
        assertEquals(AM_NEQ, null, map.remove(RING));
        // but SAM is still active
        assertEquals(AM_NEQ, SAM, map.remove(STAFF));
        // but they were both removed from the map
        verifyDeadwood(SAM, FRODO);
        advance(1);
        endTest();
    }
    
    @Test
    public void removeAgeOutDeadwoodOnly() {
        startTest("removeAgeOutDeadwoodOnly");
        map = new TestMap(TEST_AGE_OUT, true);
        map.put(RING, FRODO);
        advance(60);
        map.put(STAFF, SAM);
        advance(60);
        // FRODO aged-out so the get will add it to deadwood
        assertEquals(AM_NEQ, null, map.get(RING));
        // but SAM is still active
        assertEquals(AM_NEQ, SAM, map.remove(STAFF));
        // both are removed but only FRODO should be on deadwood
        verifyDeadwood(FRODO);
        advance(1);
        endTest();
    }

}
