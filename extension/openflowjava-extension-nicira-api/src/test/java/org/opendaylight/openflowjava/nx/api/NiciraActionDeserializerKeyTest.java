/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NiciraActionDeserializerKeyTest {

    NiciraActionDeserializerKey niciraActionDeserializerKey;

    private static final short VERSION = 4;


    /**
     * If SUBTYPE is not Uint16 exception should be thrown
     */
    @Test(expected = IllegalArgumentException.class)
    public void niciraActionDeserializerKeyTest1() {
        niciraActionDeserializerKey = new NiciraActionDeserializerKey(VERSION, -10);
    }

    /**
     * If SUBTYPE is Uint16 it should be set and version should be set also
     */
    @Test
    public void niciraActionDeserializerKeyTest2() {
        niciraActionDeserializerKey = new NiciraActionDeserializerKey(VERSION, 10);
        assertEquals(VERSION, niciraActionDeserializerKey.getVersion());
        assertEquals(10, niciraActionDeserializerKey.getSubtype());
    }


    @Test
    public void hashCodeTest() {
        niciraActionDeserializerKey = new NiciraActionDeserializerKey(VERSION, 10);
        assertEquals(1275, niciraActionDeserializerKey.hashCode());
    }

    /**
     * If input param obj is null FALSE should be returned
     */
    @Test
    public void equalsTest1() {
        Object obj = null;
        niciraActionDeserializerKey = new NiciraActionDeserializerKey(VERSION, 10);

        assertFalse(niciraActionDeserializerKey.equals(obj));
    }

    /**
     * If input param obj is NOT null but is different class FALSE should be returned
     */
    @Test
    public void equalsTest2() {
        Object obj = new Object();
        niciraActionDeserializerKey = new NiciraActionDeserializerKey(VERSION, 10);

        assertFalse(niciraActionDeserializerKey.equals(obj));
    }

    /**
     * If input param obj is same class but has different SUBTYPE value FALSE should be returned
     */
    @Test
    public void equalsTest3() {
        NiciraActionDeserializerKey obj = new NiciraActionDeserializerKey(VERSION, 9);
        niciraActionDeserializerKey = new NiciraActionDeserializerKey(VERSION, 10);

        assertFalse(niciraActionDeserializerKey.equals(obj));
    }

    /**
     * If input param obj is same class but has different VERSION value FALSE should be returned
     */
    @Test
    public void equalsTest4() {
        NiciraActionDeserializerKey obj = new NiciraActionDeserializerKey((short)5, 10);
        niciraActionDeserializerKey = new NiciraActionDeserializerKey(VERSION, 10);

        assertFalse(niciraActionDeserializerKey.equals(obj));
    }

    /**
     * If input param obj is absolutely same TRUE should be returned
     */
    @Test
    public void equalsTest5() {
        NiciraActionDeserializerKey obj = new NiciraActionDeserializerKey(VERSION, 10);
        niciraActionDeserializerKey = new NiciraActionDeserializerKey(VERSION, 10);

        assertTrue(niciraActionDeserializerKey.equals(obj));
    }

    /**
     * If input param obj is same instance TRUE should be returned
     */
    @Test
    public void equalsTest6() {
        niciraActionDeserializerKey = new NiciraActionDeserializerKey(VERSION, 10);

        assertTrue(niciraActionDeserializerKey.equals(niciraActionDeserializerKey));
    }

    @Test
    public void toStringTest() {
        niciraActionDeserializerKey = new NiciraActionDeserializerKey(VERSION, 10);
        String shouldBe = new String("NiciraActionDeserializerKey [version=4, subtype=10]");

        assertEquals(shouldBe, niciraActionDeserializerKey.toString());
    }

}