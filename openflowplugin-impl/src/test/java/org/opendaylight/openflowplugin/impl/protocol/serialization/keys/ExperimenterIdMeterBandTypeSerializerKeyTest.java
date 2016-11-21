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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandType;
import org.opendaylight.yangtools.yang.binding.DataContainer;

public class ExperimenterIdMeterBandTypeSerializerKeyTest {

    @Test
    public void testHashCode() throws Exception {
        ExperimenterIdMeterBandTypeSerializerKey<DataContainer> key1 =
                new ExperimenterIdMeterBandTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L, DataContainer.class, MeterBandType.class);
        ExperimenterIdMeterBandTypeSerializerKey<?> key2 =
                new ExperimenterIdMeterBandTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L, DataContainer.class, MeterBandType.class);
        Assert.assertTrue("Wrong equals", key1.equals(key2));
        Assert.assertTrue("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterIdMeterBandTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L, DataContainer.class, null);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterIdMeterBandTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L, null, null);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterIdMeterBandTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L, CopyTtlOutCase.class, MeterBandType.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterIdMeterBandTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L, DataContainer.class, TestBandType.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterIdMeterBandTypeSerializerKey<>(EncodeConstants.OF13_VERSION_ID, 42L, DataContainer.class, TestBandType.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());    }

    @Test
    public void testEquals() throws Exception {
        ExperimenterIdMeterBandTypeSerializerKey<?> key1 = new ExperimenterIdMeterBandTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L, DataContainer.class, null);
        ExperimenterIdMeterBandTypeSerializerKey<?> key2 = new ExperimenterIdMeterBandTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L, DataContainer.class, MeterBandType.class);

        Assert.assertTrue("Wrong equal to identical object.", key1.equals(key1));
        Assert.assertFalse("Wrong equal by actionType", key1.equals(key2));

        key2 = new ExperimenterIdMeterBandTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L, DataContainer.class, null);
        Assert.assertTrue("Wrong equal by action type", key1.equals(key2));
        key1 = new ExperimenterIdMeterBandTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L, DataContainer.class, MeterBandType.class);
        Assert.assertFalse("Wrong equal by experimenterId", key1.equals(key2));
        key2 = new ExperimenterIdMeterBandTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L, DataContainer.class, MeterBandType.class);
        Assert.assertTrue("Wrong equal by experimenterId", key1.equals(key2));
    }

    class TestBandType extends MeterBandType {
        public TestBandType(Boolean _ofpmbtDrop, Boolean _ofpmbtDscpRemark, Boolean _ofpmbtExperimenter) {
            super(_ofpmbtDrop, _ofpmbtDscpRemark, _ofpmbtExperimenter);
        }
    }
}