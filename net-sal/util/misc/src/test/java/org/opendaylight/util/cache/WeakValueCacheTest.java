/*
 * (c) Copyright 2009,2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.cache;

import java.util.ArrayList;
import java.util.List;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * This class defines unit tests for {@link org.opendaylight.util.cache.WeakValueCache}.
 *
 * @author Simon Hunt
 */
public class WeakValueCacheTest {

    @Test
    public void testBasicCreation() {
        print(EOL + "testBasicCreation():");

        WeakValueCache<String, MyValueTypeA> cache =
                new WeakValueCache<String, MyValueTypeA>(MyValueTypeA.getQ());

        cache.put("one", new MyValueTypeA(1));
                                                                    // (1) see below
        cache.put("two", new MyValueTypeA(2));
        cache.put("three", new MyValueTypeA(3));

        MyValueTypeA m = cache.get("four");
        assertNull(m);

        m = cache.get("one");
        assertNotNull(m);    // this MAY not be true, because no hard ref between here and (1)
        assertEquals(1, m.getNumber());

        print("  Got One: " + m);
    }

    @Test
    public void testTwoMapCreation() {
        print(EOL + "testTwoMapCreation():");

        WeakValueCache<String, MyValueTypeA> cacheA =
                new WeakValueCache<String, MyValueTypeA>(MyValueTypeA.getQ());

        WeakValueCache<String, MyValueTypeB> cacheB =
                new WeakValueCache<String, MyValueTypeB>(MyValueTypeB.getQ());

        cacheA.put("A-1", new MyValueTypeA(1));
        cacheA.put("A-2", new MyValueTypeA(2));

        cacheB.put("B-5", new MyValueTypeB(5));
        cacheB.put("B-6", new MyValueTypeB(6));
        cacheB.put("B-7", new MyValueTypeB(7));

        cacheA.put("A-3", new MyValueTypeA(3));

        print("  Size of cache A: " + cacheA.size());
        print("  Size of cache B: " + cacheB.size());

        print("  Getting A-1 -> " + cacheA.get("A-1"));
        print("  Getting A-2 -> " + cacheA.get("A-2"));
        print("  Getting A-3 -> " + cacheA.get("A-3"));
        print("  Getting B-5 -> " + cacheB.get("B-5"));
        print("  Getting B-6 -> " + cacheB.get("B-6"));
        print("  Getting B-7 -> " + cacheB.get("B-7"));

        print("=== G C ===");
        System.gc();

        print("  Size of cache A: " + cacheA.size());
        print("  Size of cache B: " + cacheB.size());

        print("  Getting A-1 -> " + cacheA.get("A-1"));
        print("  Getting A-2 -> " + cacheA.get("A-2"));
        print("  Getting A-3 -> " + cacheA.get("A-3"));
        print("  Getting B-5 -> " + cacheB.get("B-5"));
        print("  Getting B-6 -> " + cacheB.get("B-6"));
        print("  Getting B-7 -> " + cacheB.get("B-7"));

        print("  -- Sleeping (just a sec) --");
        delay(1000);

        print("  Size of cache A: " + cacheA.size());
        print("  Size of cache B: " + cacheB.size());

    }

    @Test
    public void testLargeCacheWithHardRefs() {
        print(EOL + "testLargeCacheWithHardRefs():");

        final int size = 200;

        WeakValueCache<Integer, MyValueTypeA> cache =
                new WeakValueCache<Integer, MyValueTypeA>(MyValueTypeA.getQ());

        List<MyValueTypeA> hardRefs = new ArrayList<MyValueTypeA>();

        for (int i=0; i<size; i++) {
            MyValueTypeA m = new MyValueTypeA(i);
            cache.put(i, m);
            if (i%10 == 0) {
                hardRefs.add(m);
            }
            print(String.format("i = %4d, cache size = %4d", i, cache.size()));
        }

        print(EOL + "hardRefs size is: " + hardRefs.size());
        for (MyValueTypeA m: hardRefs.subList(0, 5)) {
            print(" ++ " + m);
        }
        print("  ...");

        for (int i=1; i<201; i++) {
            MyValueTypeA m = cache.get(i);
            print(String.format(
                    "Requesting cache.get(%3d) / Cache Size = %3d / Reason = %s",
                    i, cache.size(), m));
            if (i%10==0) {
                print(" ====== G C =====");
                System.gc();
            }
        }

        print(EOL + "final cache size = " + cache.size());
    }


    @Test (expected = NullPointerException.class)
    public void newCacheNullQueue() {
        new WeakValueCache<Integer, MyValueTypeA>(null);
    }

    @Test (expected = NullPointerException.class)
    public void getNullKey() {
        WeakValueCache<Integer, MyValueTypeA> cache =
                new WeakValueCache<Integer, MyValueTypeA>(MyValueTypeA.getQ());
        cache.put(3, new MyValueTypeA(3));
        cache.get(null);
    }

    @Test (expected = NullPointerException.class)
    public void putNullKey() {
        WeakValueCache<Integer, MyValueTypeA> cache =
                new WeakValueCache<Integer, MyValueTypeA>(MyValueTypeA.getQ());
        cache.put(null, new MyValueTypeA(3));
    }

    @Test (expected = NullPointerException.class)
    public void putNullValue() {
        WeakValueCache<Integer, MyValueTypeA> cache =
                new WeakValueCache<Integer, MyValueTypeA>(MyValueTypeA.getQ());
        cache.put(3, null);
    }
}
