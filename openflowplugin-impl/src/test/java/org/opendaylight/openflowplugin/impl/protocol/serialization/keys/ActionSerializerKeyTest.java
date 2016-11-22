/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.keys;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCase;

public class ActionSerializerKeyTest {

    @Test
    public void testHashCode() throws Exception {
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

    @Test
    public void testEquals() throws Exception {
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

    @Test
    public void testToString() throws Exception {
        ActionSerializerKey<CopyTtlInCase> key1 = new ActionSerializerKey<>(EncodeConstants.OF10_VERSION_ID,
                CopyTtlInCase.class, 42L);

        Assert.assertEquals("Wrong toString()", "msgVersion: 1 objectType: org.opendaylight.yang.gen.v1.urn"
                + ".opendaylight.action.types.rev131112.action.Action action type:"
                + " org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action"
                + ".CopyTtlInCase experimenterID: 42", key1.toString());
    }

}