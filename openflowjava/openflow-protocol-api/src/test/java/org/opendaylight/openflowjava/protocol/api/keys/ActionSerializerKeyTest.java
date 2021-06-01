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
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for ActionSerializerKey.
 *
 * @author michal.polkorab
 */
public class ActionSerializerKeyTest {
    private static final Uint32 FORTY_TWO = Uint32.valueOf(42);
    private static final Uint32 FIFTY_FIVE = Uint32.valueOf(55);

    /**
     * Test ActionSerializerKey equals and hashCode.
     */
    @Test
    public void test() {
        ActionSerializerKey<CopyTtlInCase> key1 =
                new ActionSerializerKey<>(EncodeConstants.OF_VERSION_1_0, CopyTtlInCase.class, FORTY_TWO);
        ActionSerializerKey<?> key2 =
                new ActionSerializerKey<>(EncodeConstants.OF_VERSION_1_0, CopyTtlInCase.class, FORTY_TWO);
        Assert.assertTrue("Wrong equals", key1.equals(key2));
        Assert.assertTrue("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new ActionSerializerKey<>(EncodeConstants.OF_VERSION_1_0, CopyTtlInCase.class, (Uint32) null);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new ActionSerializerKey<>(EncodeConstants.OF_VERSION_1_0, null, (Uint32) null);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new ActionSerializerKey<>(EncodeConstants.OF_VERSION_1_0, CopyTtlOutCase.class, FORTY_TWO);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new ActionSerializerKey<>(EncodeConstants.OF_VERSION_1_0, CopyTtlInCase.class, FIFTY_FIVE);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new ActionSerializerKey<>(EncodeConstants.OF_VERSION_1_3, CopyTtlInCase.class, FIFTY_FIVE);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
    }

    /**
     * Test ActionSerializerKey equals - additional test.
     */
    @Test
    public void testEquals() {
        ActionSerializerKey<?> key1 = new ActionSerializerKey<>(EncodeConstants.OF_VERSION_1_0, null, FORTY_TWO);
        ActionSerializerKey<?> key2 = new ActionSerializerKey<>(EncodeConstants.OF_VERSION_1_0,
                CopyTtlInCase.class, FORTY_TWO);

        Assert.assertTrue("Wrong equal to identical object.", key1.equals(key1));
        Assert.assertFalse("Wrong equal by actionType", key1.equals(key2));

        key2 = new ActionSerializerKey<>(EncodeConstants.OF_VERSION_1_0, null, FORTY_TWO);
        Assert.assertTrue("Wrong equal by action type", key1.equals(key2));
        key1 = new ActionSerializerKey<>(EncodeConstants.OF_VERSION_1_0,  CopyTtlInCase.class, (Uint32) null);
        Assert.assertFalse("Wrong equal by experimenterId", key1.equals(key2));
        key2 = new ActionSerializerKey<>(EncodeConstants.OF_VERSION_1_0, CopyTtlInCase.class, (Uint32) null);
        Assert.assertTrue("Wrong equal by experimenterId", key1.equals(key2));
    }

    /**
     * Test ActionSerializerKey toString().
     */
    @Test
    public void testToString() {
        ActionSerializerKey<CopyTtlInCase> key1 = new ActionSerializerKey<>(EncodeConstants.OF_VERSION_1_0,
                CopyTtlInCase.class, FORTY_TWO);

        Assert.assertEquals("Wrong toString()", "msgVersion: 1 objectType: org.opendaylight.yang.gen.v1.urn"
                + ".opendaylight.openflow.common.action.rev150203.actions.grouping.Action action type:"
                + " org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action"
                + ".grouping.action.choice.CopyTtlInCase experimenterID: 42", key1.toString());
    }
}
