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
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.MultipartRequestInputFactory;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter.features._case.MultipartRequestMeterFeaturesBuilder;
import org.opendaylight.yangtools.yang.common.Empty;

/**
 * Unit tests for MultipartRequestMeterFeatures.
 *
 * @author michal.polkorab
 */
public class MultipartRequestMeterFeaturesTest {

    private SerializerRegistry registry;
    private OFSerializer<MultipartRequestInput> multipartFactory;

    /**
     * Initializes serializer registry and stores correct factory in field.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        multipartFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF_VERSION_1_3, MultipartRequestInput.class));
    }

    /**
     * Tests {@link MultipartRequestInputFactory} - MeterFeatures case.
     */
    @Test
    public void test() throws Exception {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setType(MultipartType.OFPMPMETERFEATURES);
        builder.setFlags(new MultipartRequestFlags(false));
        MultipartRequestMeterFeaturesCaseBuilder caseBuilder = new MultipartRequestMeterFeaturesCaseBuilder();
        MultipartRequestMeterFeaturesBuilder featuresBuilder = new MultipartRequestMeterFeaturesBuilder();
        featuresBuilder.setEmpty(Empty.value());
        caseBuilder.setMultipartRequestMeterFeatures(featuresBuilder.build());
        builder.setMultipartRequestBody(caseBuilder.build());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        multipartFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, (byte) 18, 16);
        Assert.assertEquals("Wrong type", 11, out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 0, out.readUnsignedShort());
        out.skipBytes(4); // skip padding
        Assert.assertTrue("Unexpected data", out.readableBytes() == 0);
    }
}
