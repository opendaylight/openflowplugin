/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.opendaylight.util.junit.TestTools.AM_HUH;
import static org.opendaylight.util.junit.TestTools.print;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for {@link org.opendaylight.util.MixedEnumSet}.
 *
 * @author Simon Hunt
 */
public class MixedEnumSetTest {

    private static enum EnumOne { ALPHA, BETA, GAMMA }
    private static enum EnumTwo { DELTA, EPSILON }

    private static final Set<Enum<?>> SOME_ENUMS =
            new HashSet<Enum<?>>(Arrays.asList(
                    EnumOne.ALPHA,
                    EnumOne.BETA,
                    EnumTwo.EPSILON
            ));

    private MixedEnumSet base;

    @Before
    public void setUp() {
        base = new MixedEnumSet(SOME_ENUMS);
    }

    @Test
    public void defaultConstructor() {
        MixedEnumSet set = new MixedEnumSet();
        assertEquals(AM_HUH, 0, set.size());
        assertTrue(AM_HUH, set.isEmpty());
    }


    @Test
    public void constructWithCollection() {
        assertEquals(AM_HUH, 3, base.size());
        assertTrue(AM_HUH, base.contains(EnumOne.ALPHA));
        assertTrue(AM_HUH, base.contains(EnumOne.BETA));
        assertFalse(AM_HUH, base.contains(EnumOne.GAMMA));
        assertFalse(AM_HUH, base.contains(EnumTwo.DELTA));
        assertTrue(AM_HUH, base.contains(EnumTwo.EPSILON));
        print(base);
    }


    @Test
    public void equality() {
        MixedEnumSet two = new MixedEnumSet();
        two.add(EnumOne.ALPHA);
        two.add(EnumOne.BETA);
        assertFalse(AM_HUH, base.equals(two));
        two.add(EnumTwo.EPSILON);
        assertTrue(AM_HUH, base.equals(two));
        assertEquals(AM_HUH, base.hashCode(), two.hashCode());
    }

    @Test
    public void iterating() {
        MixedEnumSet copy = new MixedEnumSet();
        assertTrue(AM_HUH, copy.isEmpty());
        Iterator<Enum<?>> it = base.iterator();
        int count = 0;
        while(it.hasNext()) {
            copy.add(it.next());
            count++;
        }
        assertEquals(AM_HUH, 3, count);
        assertTrue(AM_HUH, copy.equals(base));
    }

    @Test
    public void toArray() {
        Enum<?>[] array = base.toArray();
        assertEquals(AM_HUH, base.size(), array.length);
        for (Enum<?> e : array) {
            assertTrue(AM_HUH, base.contains(e));
        }
    }

    @Test (expected = UnsupportedOperationException.class)
    public void toArrayWithArg() {
        base.toArray(new Object[base.size()]);
    }

    @Test
    public void removingStuff() {
        assertEquals(AM_HUH, 3, base.size());
        assertFalse(AM_HUH, base.contains(EnumTwo.DELTA));
        boolean b = base.remove(EnumTwo.DELTA); // not in the list
        assertFalse(AM_HUH, b);
        assertEquals(AM_HUH, 3, base.size());

        assertTrue(AM_HUH, base.contains(EnumTwo.EPSILON));
        b = base.remove(EnumTwo.EPSILON);
        assertTrue(AM_HUH, b);
        assertEquals(AM_HUH, 2, base.size());
        assertFalse(AM_HUH, base.contains(EnumTwo.EPSILON));
    }

    @Test
    public void containsAll() {
        Collection<Object> c = new ArrayList<Object>();
        c.add(EnumTwo.EPSILON);
        c.add(Boolean.FALSE);
        assertFalse(AM_HUH, base.containsAll(c));
        c.add(EnumOne.ALPHA);
        c.remove(Boolean.FALSE);
        assertTrue(AM_HUH, base.containsAll(c));
    }

    @Test
    public void retainAll() {
        Collection<Object> c = new ArrayList<Object>();
        c.add(EnumTwo.EPSILON);
        c.add(Boolean.TRUE);
        assertEquals(AM_HUH, 3, base.size());
        base.retainAll(c);
        assertEquals(AM_HUH, 1, base.size());
        assertTrue(AM_HUH, base.contains(EnumTwo.EPSILON));
    }

    @Test
    public void removeAll() {
        Collection<Object> c = new ArrayList<Object>();
        c.add(EnumTwo.EPSILON);
        c.add(EnumOne.ALPHA);
        assertEquals(AM_HUH, 3, base.size());
        base.removeAll(c);
        assertEquals(AM_HUH, 1, base.size());
        assertFalse(AM_HUH, base.contains(EnumTwo.EPSILON));
        assertFalse(AM_HUH, base.contains(EnumOne.ALPHA));
    }

    @Test
    public void clear() {
        assertEquals(AM_HUH, 3, base.size());
        assertFalse(AM_HUH, base.isEmpty());
        base.clear();
        assertEquals(AM_HUH, 0, base.size());
        assertTrue(AM_HUH, base.isEmpty());
    }

    @Test
    public void iterateWithRemove() {
        Iterator<Enum<?>> it = base.iterator();
        while(it.hasNext()) {
            Enum<?> e = it.next();
            if (e.getClass() == EnumOne.class) {
                it.remove();
            }
        }
        assertEquals(AM_HUH, 1, base.size());
        assertTrue(AM_HUH, base.contains(EnumTwo.EPSILON));
    }
}
