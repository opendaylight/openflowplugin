/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.cache;

import java.lang.ref.ReferenceQueue;

import static org.opendaylight.util.junit.TestTools.AM_HUH;
import static org.opendaylight.util.junit.TestTools.print;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This JUnit test class tests the CachedValueWeakReference class.
 *
 * @author Simon Hunt
 */
public class CachedValueWeakReferenceTest {

    private static ReferenceQueue<? super MyValueTypeA> queue;
    private static WeakValueCache<Integer, MyValueTypeA> cache;
    private static Integer key;
    private static MyValueTypeA ref;

    @BeforeClass
    public static void classSetUp() {
        queue = MyValueTypeA.getQ();
        cache = new WeakValueCache<Integer, MyValueTypeA>(queue);
        key = 42;
        ref = new MyValueTypeA(42);
    }

    // == TESTS GO HERE ==
    @Test
    public void basic() {
        CachedValueWeakReference<Integer, MyValueTypeA> cvwr =
                new CachedValueWeakReference<Integer, MyValueTypeA>(cache,key,ref,queue);
        String str = cvwr.toString();
        print(str);
        assertTrue(AM_HUH, str.contains(key.toString()));
    }


    @Test (expected = NullPointerException.class)
    public void constructNullCache() {
        new CachedValueWeakReference<Integer, MyValueTypeA>(null,key,ref,queue);
    }

    @Test (expected = NullPointerException.class)
    public void constructNullKey() {
        new CachedValueWeakReference<Integer, MyValueTypeA>(cache,null,ref,queue);
    }

    @Test (expected = NullPointerException.class)
    public void constructNullRef() {
        new CachedValueWeakReference<Integer, MyValueTypeA>(cache,key,null,queue);
    }

    @Test (expected = NullPointerException.class)
    public void constructNullQueue() {
        new CachedValueWeakReference<Integer, MyValueTypeA>(cache,key,ref,null);
    }
}
