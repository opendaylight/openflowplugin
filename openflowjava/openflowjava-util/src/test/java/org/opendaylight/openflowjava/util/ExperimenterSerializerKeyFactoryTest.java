/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.util;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdMeterSubTypeSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdTypeSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterMeterBandSubType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test ExperimenterSerializerKeyFactory key creation.
 *
 * @author michal.polkorab
 */
public class ExperimenterSerializerKeyFactoryTest {

    @Test
    public void testCreateExperimenterMessageSerializerKey() {
        ExperimenterIdSerializerKey<?> createdKey;
        ExperimenterIdSerializerKey<?> comparationKey;

        createdKey = ExperimenterSerializerKeyFactory
                .createExperimenterMessageSerializerKey(EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(42), 1L);
        comparationKey = new ExperimenterIdTypeSerializerKey<>(EncodeConstants.OF_VERSION_1_0,
                Uint32.valueOf(42), 1L, ExperimenterDataOfChoice.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateMultipartRequestSerializerKey() {
        ExperimenterIdSerializerKey<?> createdKey;
        ExperimenterIdSerializerKey<?> comparationKey;

        createdKey = ExperimenterSerializerKeyFactory.createMultipartRequestSerializerKey(
                EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(44), 1L);
        comparationKey = new ExperimenterIdSerializerKey<>(EncodeConstants.OF_VERSION_1_0,
                Uint32.valueOf(44), ExperimenterDataOfChoice.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateMultipartRequestTFSerializerKey() {
        ExperimenterIdSerializerKey<?> createdKey;
        ExperimenterIdSerializerKey<?> comparationKey;

        createdKey = ExperimenterSerializerKeyFactory.createMultipartRequestTFSerializerKey(
                EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(45));
        comparationKey = new ExperimenterIdSerializerKey<>(EncodeConstants.OF_VERSION_1_0,
                Uint32.valueOf(45), TableFeatureProperties.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateMeterBandSerializerKey() {
        ExperimenterIdSerializerKey<?> createdKey;
        ExperimenterIdSerializerKey<?> comparationKey;

        createdKey = ExperimenterSerializerKeyFactory.createMeterBandSerializerKey(EncodeConstants.OF_VERSION_1_0,
                Uint32.valueOf(43));
        comparationKey = new ExperimenterIdSerializerKey<>(EncodeConstants.OF_VERSION_1_0,
                Uint32.valueOf(43), MeterBandExperimenterCase.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateMeterBandSubTypeSerializerKey() {
        final ExperimenterIdSerializerKey<?> createdKey;
        final ExperimenterIdSerializerKey<?> comparationKey1;
        final ExperimenterIdSerializerKey<?> comparationKey2;
        final ExperimenterIdSerializerKey<?> comparationKey3;
        final ExperimenterIdSerializerKey<?> comparationKey4;
        final ExperimenterIdSerializerKey<?> comparationKey5;

        createdKey = ExperimenterSerializerKeyFactory.createMeterBandSerializerKey(EncodeConstants.OF_VERSION_1_0,
                Uint32.valueOf(43), ExperimenterMeterBandSubType.VALUE);
        comparationKey1 = new ExperimenterIdMeterSubTypeSerializerKey<>(EncodeConstants.OF_VERSION_1_3,
                Uint32.valueOf(43), MeterBandExperimenterCase.class, ExperimenterMeterBandSubType.VALUE);
        comparationKey2 = new ExperimenterIdMeterSubTypeSerializerKey<>(EncodeConstants.OF_VERSION_1_0,
                Uint32.valueOf(42), MeterBandExperimenterCase.class, ExperimenterMeterBandSubType.VALUE);
        comparationKey3 = new ExperimenterIdMeterSubTypeSerializerKey<>(EncodeConstants.OF_VERSION_1_0,
                Uint32.valueOf(43), null, ExperimenterMeterBandSubType.VALUE);
        comparationKey4 = new ExperimenterIdMeterSubTypeSerializerKey<>(EncodeConstants.OF_VERSION_1_0,
                Uint32.valueOf(43), MeterBandExperimenterCase.class, null);
        comparationKey5 = new ExperimenterIdMeterSubTypeSerializerKey<>(EncodeConstants.OF_VERSION_1_0,
                Uint32.valueOf(43), MeterBandExperimenterCase.class, ExperimenterMeterBandSubType.VALUE);
        Assert.assertNotEquals("Wrong key created", comparationKey1, createdKey);
        Assert.assertNotEquals("Wrong key created", comparationKey2, createdKey);
        Assert.assertNotEquals("Wrong key created", comparationKey3, createdKey);
        Assert.assertNotEquals("Wrong key created", comparationKey4, createdKey);
        Assert.assertEquals("Wrong key created", comparationKey5, createdKey);
    }
}
