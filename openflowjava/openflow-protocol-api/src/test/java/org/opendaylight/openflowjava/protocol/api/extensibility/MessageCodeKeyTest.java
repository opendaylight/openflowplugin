/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.extensibility;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;

/**
 * @author michal.polkorab
 *
 */
public class MessageCodeKeyTest {

    /**
     * Test MessageCodeKey equals and hashCode
     */
    @Test
    public void test() {
        MessageCodeKey key1 =
                new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 4, BarrierInput.class);
        MessageCodeKey key2 =
                new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 4, BarrierInput.class);
        Assert.assertTrue("Wrong equals", key1.equals(key2));
        Assert.assertTrue("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 4, BarrierInput.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 4, null);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 4, BarrierOutput.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 6, BarrierInput.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
    }

    /**
     * Test MessageCodeKey equals - additional test
     */
    @Test
    public void testEquals() {
        MessageCodeKey key1 = new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 4, BarrierInput.class);

        Assert.assertTrue("Wrong equal to identical object.", key1.equals(key1));
        Assert.assertFalse("Wrong equal to null.", key1.equals(null));
        Assert.assertFalse("Wrong equal to different class.", key1.equals(new Object()));

        key1 = new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 4, null);
        MessageCodeKey key2 = new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 4, BarrierInput.class);
        Assert.assertFalse("Wrong equal by clazz.", key1.equals(key2));

        key2 = new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 4, null);
        Assert.assertTrue("Wrong equal by clazz.", key1.equals(key2));
    }

    /**
     * Test MessageCodeKey toString()
     */
    @Test
    public void testToString() {
        MessageCodeKey key1 = new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 4, BarrierInput.class);

        Assert.assertEquals("Wrong toString()", "msgVersion: 1 objectClass: org.opendaylight.yang.gen.v1.urn"
                + ".opendaylight.openflow.protocol.rev130731.BarrierInput msgType: 4", key1.toString());
    }
}