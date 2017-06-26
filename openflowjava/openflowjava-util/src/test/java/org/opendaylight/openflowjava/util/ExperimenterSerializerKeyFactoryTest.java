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

/**
 * Test ExperimenterSerializerKeyFactory key creation
 * @author michal.polkorab
 *
 */
public class ExperimenterSerializerKeyFactoryTest {

    @Test
    public void testCreateExperimenterMessageSerializerKey() throws Exception {
        ExperimenterIdSerializerKey<?> createdKey;
        ExperimenterIdSerializerKey<?> comparationKey;

        createdKey = ExperimenterSerializerKeyFactory
                .createExperimenterMessageSerializerKey(EncodeConstants.OF10_VERSION_ID, 42L, 1L);
        comparationKey = new ExperimenterIdTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID,
                42L, 1L, ExperimenterDataOfChoice.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateMultipartRequestSerializerKey() throws Exception {
        ExperimenterIdSerializerKey<?> createdKey;
        ExperimenterIdSerializerKey<?> comparationKey;

        createdKey = ExperimenterSerializerKeyFactory.createMultipartRequestSerializerKey(
                EncodeConstants.OF10_VERSION_ID, 44L, 1L);
        comparationKey = new ExperimenterIdSerializerKey<>(EncodeConstants.OF10_VERSION_ID,
                44L, ExperimenterDataOfChoice.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateMultipartRequestTFSerializerKey() throws Exception {
        ExperimenterIdSerializerKey<?> createdKey;
        ExperimenterIdSerializerKey<?> comparationKey;

        createdKey = ExperimenterSerializerKeyFactory.createMultipartRequestTFSerializerKey(
                EncodeConstants.OF10_VERSION_ID, 45L);
        comparationKey = new ExperimenterIdSerializerKey<>(EncodeConstants.OF10_VERSION_ID,
                45L, TableFeatureProperties.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateMeterBandSerializerKey() throws Exception {
        ExperimenterIdSerializerKey<?> createdKey;
        ExperimenterIdSerializerKey<?> comparationKey;

        createdKey = ExperimenterSerializerKeyFactory.createMeterBandSerializerKey(
                EncodeConstants.OF10_VERSION_ID, 43L);
        comparationKey = new ExperimenterIdSerializerKey<>(EncodeConstants.OF10_VERSION_ID,
                43L, MeterBandExperimenterCase.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateMeterBandSubTypeSerializerKey() throws Exception {
        ExperimenterIdSerializerKey<?> createdKey;
        ExperimenterIdSerializerKey<?> comparationKey1;
        ExperimenterIdSerializerKey<?> comparationKey2;
        ExperimenterIdSerializerKey<?> comparationKey3;
        ExperimenterIdSerializerKey<?> comparationKey4;
        ExperimenterIdSerializerKey<?> comparationKey5;

        createdKey = ExperimenterSerializerKeyFactory.createMeterBandSerializerKey(
                EncodeConstants.OF10_VERSION_ID, 43L, ExperimenterMeterBandSubType.class);
        comparationKey1 = new ExperimenterIdMeterSubTypeSerializerKey<>(EncodeConstants.OF13_VERSION_ID,
                43L, MeterBandExperimenterCase.class, ExperimenterMeterBandSubType.class);
        comparationKey2 = new ExperimenterIdMeterSubTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID,
                42L, MeterBandExperimenterCase.class, ExperimenterMeterBandSubType.class);
        comparationKey3 = new ExperimenterIdMeterSubTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID,
                43L, null, ExperimenterMeterBandSubType.class);
        comparationKey4 = new ExperimenterIdMeterSubTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID,
                43L, MeterBandExperimenterCase.class, null);
        comparationKey5 = new ExperimenterIdMeterSubTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID,
                43L, MeterBandExperimenterCase.class, ExperimenterMeterBandSubType.class);
        Assert.assertNotEquals("Wrong key created", comparationKey1, createdKey);
        Assert.assertNotEquals("Wrong key created", comparationKey2, createdKey);
        Assert.assertNotEquals("Wrong key created", comparationKey3, createdKey);
        Assert.assertNotEquals("Wrong key created", comparationKey4, createdKey);
        Assert.assertEquals("Wrong key created", comparationKey5, createdKey);
    }
}