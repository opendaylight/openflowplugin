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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopVlanCase;

public class NiciraActionSerializerKeyTest {


    NiciraActionSerializerKey niciraActionSerializerKey;

    private static final short VERSION = 4;


    @Test
    public void niciraActionSerializerKeyTest() {
        niciraActionSerializerKey = new NiciraActionSerializerKey(VERSION, SubtypeClass.class);

        assertEquals(VERSION, niciraActionSerializerKey.getVersion());
        assertEquals(SubtypeClass.class, niciraActionSerializerKey.getSubtype());
    }

    /**
     * If input param obj is NULL then FALSE should be returned
     */
    @Test
    public void equalsTest1() {
        Object obj = null;
        niciraActionSerializerKey = new NiciraActionSerializerKey(VERSION, SubtypeClass.class);

        assertFalse(niciraActionSerializerKey.equals(obj));
    }

    /**
     * If input param obj is NOT NULL but is instance of different class then FALSE should be returned
     */
    @Test
    public void equalsTest2() {
        Object obj = new Object();
        niciraActionSerializerKey = new NiciraActionSerializerKey(VERSION, SubtypeClass.class);

        assertFalse(niciraActionSerializerKey.equals(obj));
    }

    /**
     * If input param obj is instance of the same class but this.subtype is NULL then FALSE should be returned
     */
    @Test
    public void equalsTest3() {
        NiciraActionSerializerKey obj = new NiciraActionSerializerKey(VERSION, SubtypeClass.class);
        niciraActionSerializerKey = new NiciraActionSerializerKey(VERSION, null);

        assertFalse(niciraActionSerializerKey.equals(obj));
    }

    /**
     * If input param obj is instance of the same class but has different SUBTYPE then FALSE should be returned
     */
    @Test
    public void equalsTest4() {
        NiciraActionSerializerKey obj = new NiciraActionSerializerKey(VERSION, SubtypeClass.class);
        niciraActionSerializerKey = new NiciraActionSerializerKey(VERSION, PopVlanCase.class);

        assertFalse(niciraActionSerializerKey.equals(obj));
    }

    /**
     * If input param obj is instance of the same class but has different VERSION then FALSE should be returned
     */
    @Test
    public void equalsTest5() {
        NiciraActionSerializerKey obj = new NiciraActionSerializerKey((short)5, SubtypeClass.class);
        niciraActionSerializerKey = new NiciraActionSerializerKey(VERSION, SubtypeClass.class);

        assertFalse(niciraActionSerializerKey.equals(obj));
    }

    /**
     * If input param obj is instance of the same class and has same VERSION and SUBTYPE then TRUE should be returned
     */
    @Test
    public void equalsTest6() {
        NiciraActionSerializerKey obj = new NiciraActionSerializerKey(VERSION, SubtypeClass.class);
        niciraActionSerializerKey = new NiciraActionSerializerKey(VERSION, SubtypeClass.class);

        assertTrue(niciraActionSerializerKey.equals(obj));
    }

    /**
     * If input param obj is exactly same TRUE should be returned
     */
    @Test
    public void equalsTest7() {
        niciraActionSerializerKey = new NiciraActionSerializerKey(VERSION, SubtypeClass.class);

        assertTrue(niciraActionSerializerKey.equals(niciraActionSerializerKey));
    }



    private interface SubtypeClass extends ActionChoice {}


}