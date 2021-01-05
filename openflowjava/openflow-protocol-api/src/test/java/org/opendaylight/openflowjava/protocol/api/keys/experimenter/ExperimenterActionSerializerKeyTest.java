/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.keys.experimenter;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.ExperimenterActionSubType;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for ExperimenterActionSerializerKey.
 *
 * @author michal.polkorab
 */
public class ExperimenterActionSerializerKeyTest {
    private static final Uint32 FORTY_TWO = Uint32.valueOf(42);
    private static final Uint32 FIFTY_FIVE = Uint32.valueOf(55);

    /**
     * Test ExperimenterActionSerializerKey equals and hashCode.
     */
    @Test
    public void test() {
        ExperimenterActionSerializerKey key1 =
                new ExperimenterActionSerializerKey(EncodeConstants.OF10_VERSION_ID, FORTY_TWO, TestSubType.class);
        ExperimenterActionSerializerKey key2 =
                new ExperimenterActionSerializerKey(EncodeConstants.OF10_VERSION_ID, FORTY_TWO, TestSubType.class);
        Assert.assertTrue("Wrong equals", key1.equals(key2));
        Assert.assertTrue("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterActionSerializerKey(EncodeConstants.OF13_VERSION_ID, FORTY_TWO, TestSubType.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterActionSerializerKey(EncodeConstants.OF10_VERSION_ID, (Uint32) null, TestSubType.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterActionSerializerKey(EncodeConstants.OF10_VERSION_ID, FIFTY_FIVE, TestSubType.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterActionSerializerKey(EncodeConstants.OF10_VERSION_ID, FIFTY_FIVE, null);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterActionSerializerKey(EncodeConstants.OF10_VERSION_ID, FIFTY_FIVE, TestSubType2.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
    }

    /**
     * Test ExperimenterActionSerializerKey equals.
     */
    @Test
    public void testEquals() {
        ExperimenterActionSerializerKey key1;
        ExperimenterActionSerializerKey key2;
        key1 = new ExperimenterActionSerializerKey(EncodeConstants.OF10_VERSION_ID, FORTY_TWO, null);
        Assert.assertTrue("Wrong equal to identical object.", key1.equals(key1));
        key2 = new ExperimenterActionSerializerKey(EncodeConstants.OF10_VERSION_ID, FORTY_TWO, TestSubType2.class);
        Assert.assertFalse("Wrong equal by actionSubType.", key1.equals(key2));
        key1 = new ExperimenterActionSerializerKey(EncodeConstants.OF10_VERSION_ID, FORTY_TWO, TestSubType.class);
        Assert.assertFalse("Wrong equal by actionSubType.", key1.equals(key2));
    }


    private interface TestSubType extends ExperimenterActionSubType {
        // empty class - only used in test for comparation
    }

    private interface TestSubType2 extends ExperimenterActionSubType {
        // empty class - only used in test for comparation
    }
}