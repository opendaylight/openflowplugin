/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInputBuilder;

/**
 * @author michal.polkorab
 *
 */
public class GetFeaturesInputMessageFactoryTest {

    private static final byte FEATURES_REQUEST_MESSAGE_CODE_TYPE = 5;
    private SerializerRegistry registry;
    private OFSerializer<GetFeaturesInput> featuresFactory;

    /**
     * Initializes serializer registry and stores correct factory in field
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        featuresFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, GetFeaturesInput.class));
    }

    /**
     * Testing of {@link GetFeaturesInputMessageFactory} for correct translation from POJO
     * @throws Exception
     */
    @Test
    public void testV13() throws Exception {
        GetFeaturesInputBuilder gfib = new GetFeaturesInputBuilder();
        BufferHelper.setupHeader(gfib, EncodeConstants.OF13_VERSION_ID);
        GetFeaturesInput gfi = gfib.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        featuresFactory.serialize(gfi, out);

        BufferHelper.checkHeaderV13(out, FEATURES_REQUEST_MESSAGE_CODE_TYPE, 8);
    }

    /**
     * Testing of {@link GetFeaturesInputMessageFactory} for correct translation from POJO
     * @throws Exception
     */
    @Test
    public void testV10() throws Exception {
        GetFeaturesInputBuilder gfib = new GetFeaturesInputBuilder();
        BufferHelper.setupHeader(gfib, EncodeConstants.OF10_VERSION_ID);
        GetFeaturesInput gfi = gfib.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        featuresFactory.serialize(gfi, out);

        BufferHelper.checkHeaderV10(out, FEATURES_REQUEST_MESSAGE_CODE_TYPE, 8);
    }
}
