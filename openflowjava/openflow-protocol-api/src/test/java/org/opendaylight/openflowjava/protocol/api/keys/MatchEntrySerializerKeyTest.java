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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for MatchEntrySerializerKey.
 *
 * @author michal.polkorab
 */
public class MatchEntrySerializerKeyTest {

    /**
     * Test MatchEntrySerializerKey equals and hashCode.
     */
    @Test
    public void test() {
        MatchEntrySerializerKey<?, ?> key1 = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3, OpenflowBasicClass.VALUE, InPort.VALUE);
        MatchEntrySerializerKey<?, ?> key2 = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3, OpenflowBasicClass.VALUE, InPort.VALUE);
        Assert.assertTrue("Wrong equals", key1.equals(key2));
        Assert.assertTrue("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3,
                OpenflowBasicClass.VALUE, InPhyPort.VALUE);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3,
                Nxm0Class.VALUE, InPort.VALUE);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_0,
                OpenflowBasicClass.VALUE, InPhyPort.VALUE);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3,
                OpenflowBasicClass.VALUE, null);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3,
                null, InPhyPort.VALUE);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2.setExperimenterId(Uint32.valueOf(42L));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
    }

    /**
     * Test MatchEntrySerializerKey equals - additional test.
     */
    @Test
    public void testEquals() {
        MatchEntrySerializerKey<?, ?> key1;
        MatchEntrySerializerKey<?, ?> key2;
        key1 = new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, OpenflowBasicClass.VALUE, InPort.VALUE);
        key2 = new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, OpenflowBasicClass.VALUE, InPort.VALUE);
        Assert.assertTrue("Wrong equal to identical object.", key1.equals(key1));
        Assert.assertFalse("Wrong equal to different class.", key1.equals(new Object()));

        Uint32 expId2 = Uint32.valueOf(123456789L);

        key1.setExperimenterId(null);
        key2.setExperimenterId(expId2);
        Assert.assertFalse("Wrong equal by experimenterId", key1.equals(key2));

        Uint32 expId1 = Uint32.valueOf(987654331L);
        key1.setExperimenterId(expId1);
        Assert.assertFalse("Wrong equal by experimenterId", key1.equals(key2));
        key1 = new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, null, InPort.VALUE);
        key1.setExperimenterId(expId2);
        Assert.assertFalse("Wrong equal by oxmClass", key1.equals(key2));
        key2 = new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, null, InPort.VALUE);
        key2.setExperimenterId(expId2);
        Assert.assertTrue("Wrong equal by oxmClass", key1.equals(key2));
        key1 = new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, OpenflowBasicClass.VALUE, null);
        key1.setExperimenterId(expId2);
        Assert.assertFalse("Wrong equal by oxmField", key1.equals(key2));
        key2 = new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, OpenflowBasicClass.VALUE, null);
        key2.setExperimenterId(expId2);
        Assert.assertTrue("Wrong equal by oxmField", key1.equals(key2));
    }

    /**
     * Test MatchEntrySerializerKey toString().
     */
    @Test
    public void testToString() {
        MatchEntrySerializerKey<?, ?> key1;
        key1 = new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, OpenflowBasicClass.VALUE, InPort.VALUE);

        Assert.assertEquals("Wrong toString()", """
            msgVersion: 4 objectType: org.opendaylight.yang.gen.v1.urn\
            .opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry \
            oxm_class: org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225\
            .OpenflowBasicClass oxm_field: org.opendaylight.yang.gen.v1.urn.opendaylight.openflow\
            .oxm.rev150225.InPort experimenterID: null""", key1.toString());
    }
}
