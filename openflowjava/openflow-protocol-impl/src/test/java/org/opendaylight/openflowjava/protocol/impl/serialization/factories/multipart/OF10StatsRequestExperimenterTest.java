/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.serialization.factories.multipart;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdTypeSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.OF10StatsRequestInputFactory;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.experimenter._case.MultipartRequestExperimenterBuilder;

/**
 * Unit tests for OF10StatsRequestExperimenter.
 *
 * @author michal.polkorab
 */
@RunWith(MockitoJUnitRunner.class)
public class OF10StatsRequestExperimenterTest {

    @Mock SerializerRegistry mockRegistry;
    @Mock OFSerializer<ExperimenterDataOfChoice> serializer;
    @Mock
    private ExperimenterDataOfChoice vendorData;

    /**
     * Testing OF10StatsRequestInputFactory (Experimenter) for correct serialization.
     */
    @Test
    public void testExperimenter() throws Exception {
        Mockito.when(mockRegistry.getSerializer(
                ArgumentMatchers.<ExperimenterIdTypeSerializerKey<ExperimenterDataOfChoice>>any()))
                .thenReturn(serializer);
        OF10StatsRequestInputFactory multipartFactory = new OF10StatsRequestInputFactory();
        multipartFactory.injectSerializerRegistry(mockRegistry);
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setType(MultipartType.OFPMPEXPERIMENTER);
        builder.setFlags(new MultipartRequestFlags(false));
        final MultipartRequestExperimenterCaseBuilder caseBuilder = new MultipartRequestExperimenterCaseBuilder();
        MultipartRequestExperimenterBuilder expBuilder = new MultipartRequestExperimenterBuilder();
        expBuilder.setExperimenter(new ExperimenterId(42L));
        expBuilder.setExpType(21L);
        expBuilder.setExperimenterDataOfChoice(vendorData);
        caseBuilder.setMultipartRequestExperimenter(expBuilder.build());
        builder.setMultipartRequestBody(caseBuilder.build());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        multipartFactory.serialize(message, out);

        BufferHelper.checkHeaderV10(out, (byte) 16, 16);
        Assert.assertEquals("Wrong type", 65535, out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 0, out.readUnsignedShort());
        Mockito.verify(serializer, Mockito.times(1)).serialize(vendorData, out);
    }
}
