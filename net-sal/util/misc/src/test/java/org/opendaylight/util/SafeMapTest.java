/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.junit.Test;

import java.util.Set;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;

/**
 * This JUnit test class tests the SafeMap class.
 *
 * @author Simon Hunt
 * @author Frank Wood
 */
public class SafeMapTest {

    private static final String M_WDK = "wrong default key";
    private static final String M_WFK = "wrong first key";
    private static final String M_WDV = "wrong default value";
    private static final String M_WV = "wrong value";
    private static final String M_MK = "missing key";

    private static final String FOO = "foo";
    private static final String ONE = "one";
    private static final String TWO = "two";
    private static final String THREE = "three";

    private enum Greek { ALPHA, BETA, GAMMA, DELTA, EPSILON }
    private enum Roman { I, II, III, IV, V, XX }

    private SafeMap<String, Integer> map;
    private SafeMap<Greek, Roman> emap;

    @Test
    public void basic() {
        print(EOL + "basic()");
        map = new SafeMap.Builder<String, Integer>(42).build();
        print(map);
        assertEquals(AM_UXS, 0, map.size());
        assertEquals(M_WDV, (Integer)42, map.get(FOO));
        assertEquals(M_WDV, (Integer)42, map.get(null));
        assertEquals(M_WDV, (Integer)42, map.getDefaultValue());
        assertNull(M_WDK, map.getDefaultKey());
        assertEquals(AM_NEQ, "[defaultValue=42, defaultKey=null, {}]", map.toString());
    }

    @Test
    public void OneTwoThree() {
        print(EOL + "OneTwoThree()");
        map = new SafeMap.Builder<String, Integer>(0)
                .add(ONE, 1).add(TWO, 2).add(THREE, 3).build();
        print(map);
        assertEquals(AM_UXS, 3, map.size());
        assertEquals(M_WV, (Integer)1, map.get(ONE));
        assertEquals(M_WV, (Integer)2, map.get(TWO));
        assertEquals(M_WV, (Integer)3, map.get(THREE));
        assertEquals(M_WDV, (Integer)0, map.get(FOO));
        assertEquals(M_WDV, (Integer)0, map.getDefaultValue());
        assertEquals(M_WDV, (Integer)0, map.get(null));
        assertNull(M_WDK, map.getDefaultKey());
        assertEquals(AM_NEQ, "[defaultValue=0, defaultKey=null, {one=1, three=3, two=2}]", map.toString());
    }

    @Test
    public void nullDefault() {
        try {
            map = new SafeMap.Builder<String, Integer>(null).build();
            fail(AM_NOEX);
        } catch (NullPointerException e) {
            assertTrue(AM_HUH, e.getMessage().contains(SafeMap.E_NULL_DEFAULT));
        }
    }

    @Test
    public void addNullKey() {
        try {
            map = new SafeMap.Builder<String, Integer>(0).add(null, 1).build();
            fail(AM_NOEX);
        } catch (NullPointerException e) {
            assertTrue(AM_HUH, e.getMessage().contains(SafeMap.E_NULL_KEY));
        }
    }

    @Test
    public void addNullValue() {
        try {
            map = new SafeMap.Builder<String, Integer>(0).add(ONE, null).build();
            fail(AM_NOEX);
        } catch (NullPointerException e) {
            assertTrue(AM_HUH, e.getMessage().contains(SafeMap.E_NULL_VALUE));
        }
    }

    @Test
    public void defaultKey() {
        print(EOL + "nullDefaultKey()");
        // since default key is optional, and if not set is null (by default), then explicitly
        // setting it to null should be acceptable too.
        map = new SafeMap.Builder<String, Integer>(0)
                .add(ONE, 1)
                .add(TWO, 2)
                .defaultKey(null)
                .build();
        print(map);
        assertNull(M_WDK, map.getDefaultKey());

        map = new SafeMap.Builder<String, Integer>(0)
                .add(ONE, 1)
                .add(TWO, 2)
                .defaultKey(ONE)
                .build();
        print(map);
        assertEquals(M_WDK, ONE, map.getDefaultKey());
        assertEquals(M_WDK, ONE, map.getFirstKey(9999));
    }

    @Test
    public void enumVersion() {
        print(EOL + "enumVersion()");
        emap = new SafeMap.Builder<Greek, Roman>(Roman.XX)
                .add(Greek.ALPHA, Roman.I)
                .add(Greek.BETA, Roman.II)
                .add(Greek.GAMMA, Roman.III)
                .build();
        print(emap);
        assertEquals(AM_UXS, 3, emap.size());
        assertEquals(M_WDV, Roman.XX, emap.getDefaultValue());
        assertEquals(M_WDV, Roman.XX, emap.get(null));
        assertEquals(M_WV, Roman.I, emap.get(Greek.ALPHA));
        assertEquals(M_WV, Roman.II, emap.get(Greek.BETA));
        assertEquals(M_WV, Roman.III, emap.get(Greek.GAMMA));
        assertEquals(M_WDV, Roman.XX, emap.get(Greek.DELTA));
        assertEquals(M_WDV, Roman.XX, emap.get(Greek.EPSILON));
    }

    @Test
    public void firstKey() {
        print(EOL + "firstKey()");
        emap = new SafeMap.Builder<Greek, Roman>(Roman.XX)
                .add(Greek.ALPHA, Roman.I)
                .add(Greek.BETA, Roman.II)
                .add(Greek.GAMMA, Roman.III)
                .add(Greek.DELTA, Roman.III)
                .add(Greek.EPSILON, Roman.III)
                .defaultKey(Greek.ALPHA)
                .build();
        print(emap);
        assertEquals(M_WFK, Greek.ALPHA, emap.getFirstKey(Roman.I));
        assertEquals(M_WFK, Greek.BETA, emap.getFirstKey(Roman.II));
        assertEquals(M_WFK, Greek.GAMMA, emap.getFirstKey(Roman.III));
        assertEquals(M_WFK, Greek.ALPHA, emap.getFirstKey(Roman.IV));
        assertEquals(M_WFK, Greek.ALPHA, emap.getFirstKey(Roman.V));

        emap = new SafeMap.Builder<Greek, Roman>(Roman.XX)
                .add(Greek.ALPHA, Roman.I)
                .add(Greek.BETA, Roman.II)
                .add(Greek.GAMMA, Roman.III)
                .add(Greek.DELTA, Roman.III)
                .add(Greek.EPSILON, Roman.III)
                .build();
        print(emap);
        assertEquals(M_WFK, Greek.ALPHA, emap.getFirstKey(Roman.I));
        assertEquals(M_WFK, Greek.BETA, emap.getFirstKey(Roman.II));
        assertEquals(M_WFK, Greek.GAMMA, emap.getFirstKey(Roman.III));
        assertNull(M_WFK, emap.getFirstKey(Roman.IV));
        assertNull(M_WFK, emap.getFirstKey(Roman.V));
    }

    @Test
    public void getAllKeys() {
        print(EOL + "getAllKeys()");
        Set<Greek> keys;
        SafeMap.Builder<Greek, Roman> builder = new SafeMap.Builder<Greek, Roman>(Roman.XX);
        emap = builder.build();
        print(emap);

        keys = emap.getAllKeys(null);
        assertEquals(AM_UXS, 0, keys.size());
        keys = emap.getAllKeys(Roman.XX);
        assertEquals(AM_UXS, 0, keys.size());
        keys = emap.getAllKeys(Roman.III); 
        assertEquals(AM_UXS, 0, keys.size());

        builder.add(Greek.ALPHA, Roman.I)
                .add(Greek.BETA, Roman.I)
                .add(Greek.GAMMA, Roman.V)
                .add(Greek.DELTA, Roman.V)
                .add(Greek.EPSILON, Roman.V);
        emap = builder.build();
        print(emap);

        keys = emap.getAllKeys(null);
        print(keys);
        assertEquals(AM_UXS, 0, keys.size());
        keys = emap.getAllKeys(Roman.XX);
        assertEquals(AM_UXS, 0, keys.size());

        keys = emap.getAllKeys(Roman.I);
        print(keys);
        assertEquals(AM_UXS, 2, keys.size());
        assertTrue(M_MK, keys.contains(Greek.ALPHA));
        assertTrue(M_MK, keys.contains(Greek.BETA));

        keys = emap.getAllKeys(Roman.V);
        print(keys);
        assertEquals(AM_UXS, 3, keys.size());
        assertTrue(M_MK, keys.contains(Greek.GAMMA));
        assertTrue(M_MK, keys.contains(Greek.DELTA));
        assertTrue(M_MK, keys.contains(Greek.EPSILON));
    }


    @Test
    public void immutable() {
        print(EOL + "immutable()");
        SafeMap.Builder<Roman, Greek> builder = new SafeMap.Builder<Roman, Greek>(Greek.EPSILON);
        builder.add(Roman.I, Greek.ALPHA).add(Roman.II, Greek.BETA);
        SafeMap<Roman, Greek> firstMap = builder.build();
        print(firstMap);
        validateFirstMap(firstMap);

        // now tweak the builder and build a second map
        builder.add(Roman.III, Greek.GAMMA).defaultKey(Roman.XX);
        SafeMap<Roman, Greek> secondMap = builder.build();
        print(secondMap);
        assertEquals(AM_UXS, 3, secondMap.size());
        assertEquals(M_WDK, Roman.XX, secondMap.getDefaultKey());
        // but it should have no effect on the first map        
        validateFirstMap(firstMap);
    }

    private void validateFirstMap(SafeMap<Roman, Greek> map) {
        assertEquals(AM_UXS, 2, map.size());
        assertEquals(M_WDV, Greek.EPSILON, map.getDefaultValue());
        assertNull(M_WDK, map.getDefaultKey());
        assertEquals(M_WV, Greek.ALPHA, map.get(Roman.I));
        assertEquals(M_WV, Greek.BETA, map.get(Roman.II));
        assertEquals(M_WDV, Greek.EPSILON, map.get(Roman.III));
        assertEquals(M_WDV, Greek.EPSILON, map.get(Roman.XX));
        assertEquals(M_WFK, Roman.I, map.getFirstKey(Greek.ALPHA));
        assertEquals(M_WFK, Roman.II, map.getFirstKey(Greek.BETA));
        assertNull(M_WDK, map.getFirstKey(Greek.GAMMA));
        assertNull(M_WDK, map.getFirstKey(Greek.DELTA));
        assertNull(M_WDK, map.getFirstKey(Greek.EPSILON));
        assertNull(M_WDK, map.getFirstKey(null));

    }
}
