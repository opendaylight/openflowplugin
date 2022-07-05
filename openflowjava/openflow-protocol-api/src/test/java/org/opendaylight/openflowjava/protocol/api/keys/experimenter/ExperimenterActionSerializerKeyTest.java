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
                new ExperimenterActionSerializerKey(EncodeConstants.OF_VERSION_1_0, FORTY_TWO, TestSubType.VALUE);
        ExperimenterActionSerializerKey key2 =
                new ExperimenterActionSerializerKey(EncodeConstants.OF_VERSION_1_0, FORTY_TWO, TestSubType.VALUE);
        Assert.assertTrue("Wrong equals", key1.equals(key2));
        Assert.assertTrue("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterActionSerializerKey(EncodeConstants.OF_VERSION_1_3, FORTY_TWO, TestSubType.VALUE);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterActionSerializerKey(EncodeConstants.OF_VERSION_1_0, (Uint32) null, TestSubType.VALUE);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterActionSerializerKey(EncodeConstants.OF_VERSION_1_0, FIFTY_FIVE, TestSubType.VALUE);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterActionSerializerKey(EncodeConstants.OF_VERSION_1_0, FIFTY_FIVE, null);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterActionSerializerKey(EncodeConstants.OF_VERSION_1_0, FIFTY_FIVE, TestSubType2.VALUE);
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
        key1 = new ExperimenterActionSerializerKey(EncodeConstants.OF_VERSION_1_0, FORTY_TWO, null);
        Assert.assertTrue("Wrong equal to identical object.", key1.equals(key1));
        key2 = new ExperimenterActionSerializerKey(EncodeConstants.OF_VERSION_1_0, FORTY_TWO, TestSubType2.VALUE);
        Assert.assertFalse("Wrong equal by actionSubType.", key1.equals(key2));
        key1 = new ExperimenterActionSerializerKey(EncodeConstants.OF_VERSION_1_0, FORTY_TWO, TestSubType.VALUE);
        Assert.assertFalse("Wrong equal by actionSubType.", key1.equals(key2));
    }


    private interface TestSubType extends ExperimenterActionSubType {
        TestSubType VALUE = () -> TestSubType.class;

        @Override
        Class<? extends TestSubType> implementedInterface();
        // empty class - only used in test for comparation
    }

    private interface TestSubType2 extends ExperimenterActionSubType {
        TestSubType2 VALUE = () -> TestSubType2.class;

        @Override
        Class<? extends TestSubType2> implementedInterface();
    }
}
