/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core.connection;

import org.junit.Test;
import org.junit.Assert;
import org.opendaylight.openflowjava.protocol.impl.core.connection.RpcResponseKey;

/**
 *
 * @author madamjak
 *
 */
public class RpcResponseKeyTest {

    /**
     * Test equals (xid is not tested)
     */
    @Test
    public void testEquals(){

        long xid1 = 12L;
        long xid2 = 66L;
        String outputClazz1 = "Clazz01";
        String outputClazz2 = "Clazz02";
        RpcResponseKey key1 = new RpcResponseKey(xid1, null);
        RpcResponseKey key2 = new RpcResponseKey(xid2, outputClazz2);

        Assert.assertTrue("Wrong equal to same obejct.", key1.equals(key1));
        Assert.assertFalse("Wrong equal to null.", key1.equals(null));
        Assert.assertFalse("Wrong equal to different type.", key1.equals(new Object()));
        Assert.assertFalse("Wrong equal by outputClazz.", key1.equals(key2));

        key1 = new RpcResponseKey(xid1, outputClazz1);
        Assert.assertFalse("Wrong equal by outputClazz.", key1.equals(key2));
        key2 = new RpcResponseKey(xid2, outputClazz1);
        Assert.assertFalse("Wrong equal.", key1.equals(key2));
        key1 = new RpcResponseKey(xid2, outputClazz1);
        Assert.assertTrue("Wrong equal.", key1.equals(key2));
    }

    /**
     * Test getters
     */
    @Test
    public void testGetters(){

        long xid1 = 12L;
        String outputClazz1 = "Clazz01";
        RpcResponseKey key1 = new RpcResponseKey(xid1, outputClazz1);

        Assert.assertTrue("Wrong getXid",key1.getXid() == xid1);
        Assert.assertTrue("Wrong getOutputClazz",key1.getOutputClazz() == outputClazz1);
    }
}
