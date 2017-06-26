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
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdTypeDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.property.header.QueueProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;

/**
 * @author michal.polkorab
 */
public class ExperimenterDeserializerKeyFactoryTest {

    @Test
    public void testCreateExperimenterErrorDeserializerKey() throws Exception {
        ExperimenterIdDeserializerKey createdKey;
        ExperimenterIdDeserializerKey comparationKey;

        createdKey = ExperimenterDeserializerKeyFactory
                .createExperimenterErrorDeserializerKey(EncodeConstants.OF10_VERSION_ID, 42L);
        comparationKey = new ExperimenterIdDeserializerKey(EncodeConstants.OF10_VERSION_ID, 42L, ErrorMessage.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateExperimenterMessageDeserializerKey() throws Exception {
        ExperimenterIdDeserializerKey createdKey;
        ExperimenterIdDeserializerKey comparationKey;

        createdKey = ExperimenterDeserializerKeyFactory.createExperimenterMessageDeserializerKey(
                EncodeConstants.OF10_VERSION_ID, 43L, 1L);
        comparationKey = new ExperimenterIdTypeDeserializerKey(EncodeConstants.OF10_VERSION_ID,
                43L, 1L, ExperimenterDataOfChoice.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateMultipartReplyMessageDeserializerKey() throws Exception {
        ExperimenterIdDeserializerKey createdKey;
        ExperimenterIdDeserializerKey comparationKey;

        createdKey = ExperimenterDeserializerKeyFactory.createMultipartReplyMessageDeserializerKey(
                EncodeConstants.OF10_VERSION_ID, 45L, 1L);
        comparationKey = new ExperimenterIdDeserializerKey(EncodeConstants.OF10_VERSION_ID,
                45L, ExperimenterDataOfChoice.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateMultipartReplyTFDeserializerKey() throws Exception {
        ExperimenterIdDeserializerKey createdKey;
        ExperimenterIdDeserializerKey comparationKey;

        createdKey = ExperimenterDeserializerKeyFactory.createMultipartReplyTFDeserializerKey(
                EncodeConstants.OF10_VERSION_ID, 46L);
        comparationKey = new ExperimenterIdDeserializerKey(EncodeConstants.OF10_VERSION_ID,
                46L, TableFeatureProperties.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateQueuePropertyDeserializerKey() throws Exception {
        ExperimenterIdDeserializerKey createdKey;
        ExperimenterIdDeserializerKey comparationKey;

        createdKey = ExperimenterDeserializerKeyFactory.createQueuePropertyDeserializerKey(
                EncodeConstants.OF10_VERSION_ID, 47L);
        comparationKey = new ExperimenterIdDeserializerKey(EncodeConstants.OF10_VERSION_ID,
                47L, QueueProperty.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateMeterBandDeserializerKey() throws Exception {
        ExperimenterIdDeserializerKey createdKey;
        ExperimenterIdDeserializerKey comparationKey;

        createdKey = ExperimenterDeserializerKeyFactory.createMeterBandDeserializerKey(
                EncodeConstants.OF10_VERSION_ID, 44L);
        comparationKey = new ExperimenterIdDeserializerKey(EncodeConstants.OF10_VERSION_ID,
                44L, MeterBandExperimenterCase.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateVendorMessageDeserializerKey() throws Exception {
        ExperimenterIdDeserializerKey createdKey;
        ExperimenterIdDeserializerKey comparationKey;

        createdKey = ExperimenterDeserializerKeyFactory.createVendorMessageDeserializerKey(
                EncodeConstants.OF10_VERSION_ID, 43L);
        comparationKey = new ExperimenterIdDeserializerKey(EncodeConstants.OF10_VERSION_ID,
                43L, ExperimenterDataOfChoice.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateMultipartReplyVendorMessageDeserializerKey() throws Exception {
        ExperimenterIdDeserializerKey createdKey;
        ExperimenterIdDeserializerKey comparationKey;

        createdKey = ExperimenterDeserializerKeyFactory.createMultipartReplyVendorMessageDeserializerKey(
                EncodeConstants.OF10_VERSION_ID, 43L);
        comparationKey = new ExperimenterIdDeserializerKey(EncodeConstants.OF10_VERSION_ID,
                43L, ExperimenterDataOfChoice.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }
}