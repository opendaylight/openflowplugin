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
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for ExperimenterDeserializerKeyFactory.
 *
 * @author michal.polkorab
 */
public class ExperimenterDeserializerKeyFactoryTest {

    @Test
    public void testCreateExperimenterErrorDeserializerKey() {
        ExperimenterIdDeserializerKey createdKey;
        ExperimenterIdDeserializerKey comparationKey;

        createdKey = ExperimenterDeserializerKeyFactory.createExperimenterErrorDeserializerKey(
                EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(42));
        comparationKey = new ExperimenterIdDeserializerKey(EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(42),
                ErrorMessage.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateExperimenterMessageDeserializerKey() {
        ExperimenterIdDeserializerKey createdKey;
        ExperimenterIdDeserializerKey comparationKey;

        createdKey = ExperimenterDeserializerKeyFactory.createExperimenterMessageDeserializerKey(
                EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(43), 1L);
        comparationKey = new ExperimenterIdTypeDeserializerKey(EncodeConstants.OF_VERSION_1_0,
                Uint32.valueOf(43), 1L, ExperimenterDataOfChoice.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateMultipartReplyMessageDeserializerKey() {
        ExperimenterIdDeserializerKey createdKey;
        ExperimenterIdDeserializerKey comparationKey;

        createdKey = ExperimenterDeserializerKeyFactory.createMultipartReplyMessageDeserializerKey(
                EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(45), 1L);
        comparationKey = new ExperimenterIdDeserializerKey(EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(45),
                ExperimenterDataOfChoice.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateMultipartReplyTFDeserializerKey() {
        ExperimenterIdDeserializerKey createdKey;
        ExperimenterIdDeserializerKey comparationKey;

        createdKey = ExperimenterDeserializerKeyFactory.createMultipartReplyTFDeserializerKey(
                EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(46));
        comparationKey = new ExperimenterIdDeserializerKey(EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(46),
                TableFeatureProperties.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateQueuePropertyDeserializerKey() {
        ExperimenterIdDeserializerKey createdKey;
        ExperimenterIdDeserializerKey comparationKey;

        createdKey = ExperimenterDeserializerKeyFactory.createQueuePropertyDeserializerKey(
                EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(47));
        comparationKey = new ExperimenterIdDeserializerKey(EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(47),
                QueueProperty.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateMeterBandDeserializerKey() {
        ExperimenterIdDeserializerKey createdKey;
        ExperimenterIdDeserializerKey comparationKey;

        createdKey = ExperimenterDeserializerKeyFactory.createMeterBandDeserializerKey(
                EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(44));
        comparationKey = new ExperimenterIdDeserializerKey(EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(44),
                MeterBandExperimenterCase.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateVendorMessageDeserializerKey() {
        ExperimenterIdDeserializerKey createdKey;
        ExperimenterIdDeserializerKey comparationKey;

        createdKey = ExperimenterDeserializerKeyFactory.createVendorMessageDeserializerKey(
                EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(43));
        comparationKey = new ExperimenterIdDeserializerKey(EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(43),
                ExperimenterDataOfChoice.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }

    @Test
    public void testCreateMultipartReplyVendorMessageDeserializerKey() {
        ExperimenterIdDeserializerKey createdKey;
        ExperimenterIdDeserializerKey comparationKey;

        createdKey = ExperimenterDeserializerKeyFactory.createMultipartReplyVendorMessageDeserializerKey(
                EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(43));
        comparationKey = new ExperimenterIdDeserializerKey(EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(43),
                ExperimenterDataOfChoice.class);
        Assert.assertEquals("Wrong key created", comparationKey, createdKey);
    }
}
