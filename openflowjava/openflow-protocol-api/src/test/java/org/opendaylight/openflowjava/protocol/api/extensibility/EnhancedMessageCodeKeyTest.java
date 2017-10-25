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
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;

/**
 * @author michal.polkorab
 *
 */
public class EnhancedMessageCodeKeyTest {

    /**
     * Test EnhancedMessageCodeKey equals and hashCode
     */
    @Test
    public void test() {
        EnhancedMessageCodeKey key1 =
                new EnhancedMessageCodeKey(EncodeConstants.OF10_VERSION_ID, 4, 8, BarrierInput.class);
        EnhancedMessageCodeKey key2 =
                new EnhancedMessageCodeKey(EncodeConstants.OF10_VERSION_ID, 4, 8, BarrierInput.class);
        Assert.assertTrue("Wrong equals", key1.equals(key2));
        Assert.assertTrue("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new EnhancedMessageCodeKey(EncodeConstants.OF13_VERSION_ID, 4, 8, BarrierInput.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new EnhancedMessageCodeKey(EncodeConstants.OF10_VERSION_ID, 4, 8, null);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new EnhancedMessageCodeKey(EncodeConstants.OF10_VERSION_ID, 4, 8, BarrierOutput.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new EnhancedMessageCodeKey(EncodeConstants.OF10_VERSION_ID, 6, 8, BarrierInput.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new EnhancedMessageCodeKey(EncodeConstants.OF10_VERSION_ID, 4, 16, BarrierInput.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
    }

    /**
     * Test EnhancedMessageTypeKey equals - additional test
     */
    @Test
    public void testEquals() {
        EnhancedMessageCodeKey key1 =
                 new EnhancedMessageCodeKey(EncodeConstants.OF10_VERSION_ID, 4, 8, BarrierInput.class);

        Assert.assertTrue("Wrong equal to identical object.", key1.equals(key1));
        Assert.assertFalse("Wrong equal to different class.", key1.equals(new Object()));
    }

    /**
     * Test EnhancedMessageCodeKey toString()
     */
    @Test
    public void testToString() {
        EnhancedMessageCodeKey key1 =
                new EnhancedMessageCodeKey(EncodeConstants.OF10_VERSION_ID, 4, 8, BarrierInput.class);

        Assert.assertEquals("Wrong toString()", "msgVersion: 1 objectClass: org.opendaylight.yang.gen.v1.urn.opendaylight"
                + ".openflow.protocol.rev130731.BarrierInput msgType: 4 msgType2: 8", key1.toString());
    }
}
