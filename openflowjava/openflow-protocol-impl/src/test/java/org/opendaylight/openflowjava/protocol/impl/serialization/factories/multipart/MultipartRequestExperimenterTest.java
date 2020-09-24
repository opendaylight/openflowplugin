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
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdTypeSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.MultipartRequestInputFactory;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.experimenter._case.MultipartRequestExperimenterBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for MultipartRequestExperimenter.
 *
 * @author michal.polkorab
 */
@RunWith(MockitoJUnitRunner.class)
public class MultipartRequestExperimenterTest {

    @Mock SerializerRegistry mockRegistry;
    @Mock OFSerializer<ExperimenterDataOfChoice> serializer;

    @Mock ExperimenterDataOfChoice vendorData;

    /**
     * Testing OF10StatsRequestInputFactory (Experimenter) for correct serialization.
     */
    @Test
    public void testExperimenter() throws Exception {
        Mockito.when(mockRegistry.getSerializer(
                ArgumentMatchers.<ExperimenterIdTypeSerializerKey<ExperimenterDataOfChoice>>any()))
                .thenReturn(serializer);
        MultipartRequestInputFactory multipartFactory = new MultipartRequestInputFactory();
        multipartFactory.injectSerializerRegistry(mockRegistry);
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setType(MultipartType.OFPMPEXPERIMENTER);
        builder.setFlags(new MultipartRequestFlags(false));
        final MultipartRequestExperimenterCaseBuilder caseBuilder = new MultipartRequestExperimenterCaseBuilder();
        MultipartRequestExperimenterBuilder expBuilder = new MultipartRequestExperimenterBuilder();
        expBuilder.setExperimenter(new ExperimenterId(Uint32.valueOf(42)));
        expBuilder.setExpType(Uint32.valueOf(21));
        expBuilder.setExperimenterDataOfChoice(vendorData);
        caseBuilder.setMultipartRequestExperimenter(expBuilder.build());
        builder.setMultipartRequestBody(caseBuilder.build());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        multipartFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, (byte) 18, 24);
        Assert.assertEquals("Wrong type", 65535, out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 0, out.readUnsignedShort());
        Mockito.verify(serializer, Mockito.times(1)).serialize(vendorData, out);
    }
}
