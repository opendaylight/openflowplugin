/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.keys;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlInCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlOutCase;

/**
 * @author michal.polkorab
 *
 */
public class ActionSerializerKeyTest {

    /**
     * Test ActionSerializerKey equals and hashCode
     */
    @Test
    public void test() {
        ActionSerializerKey<CopyTtlInCase> key1 =
                new ActionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, CopyTtlInCase.class, 42L);
        ActionSerializerKey<?> key2 =
                new ActionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, CopyTtlInCase.class, 42L);
        Assert.assertTrue("Wrong equals", key1.equals(key2));
        Assert.assertTrue("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new ActionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, CopyTtlInCase.class, null);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new ActionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, null, null);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new ActionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, CopyTtlOutCase.class, 42L);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new ActionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, CopyTtlInCase.class, 55L);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new ActionSerializerKey<>(EncodeConstants.OF13_VERSION_ID, CopyTtlInCase.class, 55L);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
    }

    /**
     * Test ActionSerializerKey equals - additional test
     */
    @Test
    public void testEquals(){
        ActionSerializerKey<?> key1 = new ActionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, null, 42L);
        ActionSerializerKey<?> key2 = new ActionSerializerKey<>(EncodeConstants.OF10_VERSION_ID,
                CopyTtlInCase.class, 42L);

        Assert.assertTrue("Wrong equal to identical object.", key1.equals(key1));
        Assert.assertFalse("Wrong equal by actionType", key1.equals(key2));

        key2 = new ActionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, null, 42L);
        Assert.assertTrue("Wrong equal by action type", key1.equals(key2));
        key1 = new ActionSerializerKey<>(EncodeConstants.OF10_VERSION_ID,  CopyTtlInCase.class, null);
        Assert.assertFalse("Wrong equal by experimenterId", key1.equals(key2));
        key2 = new ActionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, CopyTtlInCase.class, null);
        Assert.assertTrue("Wrong equal by experimenterId", key1.equals(key2));
    }

    /**
     * Test ActionSerializerKey toString()
     */
    @Test
    public void testToString(){
        ActionSerializerKey<CopyTtlInCase> key1 = new ActionSerializerKey<>(EncodeConstants.OF10_VERSION_ID,
                CopyTtlInCase.class, 42L);

        Assert.assertEquals("Wrong toString()", "msgVersion: 1 objectType: org.opendaylight.yang.gen.v1.urn"
                + ".opendaylight.openflow.common.action.rev150203.actions.grouping.Action action type:"
                + " org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action"
                + ".grouping.action.choice.CopyTtlInCase experimenterID: 42", key1.toString());
    }
}
