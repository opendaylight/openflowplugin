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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigInputBuilder;

/**
 * @author michal.polkorab
 *
 */
public class GetConfigInputMessageFactoryTest {

    private static final byte GET_CONFIG_REQUEST_MESSAGE_CODE_TYPE = 7;
    private SerializerRegistry registry;
    private OFSerializer<GetConfigInput> getConfigFactory;

    /**
     * Initializes serializer registry and stores correct factory in field
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        getConfigFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, GetConfigInput.class));
    }

    /**
     * Testing of {@link GetConfigInputMessageFactory} for correct translation from POJO
     * @throws Exception
     */
    @Test
    public void testV13() throws Exception {
        GetConfigInputBuilder gcib = new GetConfigInputBuilder();
        BufferHelper.setupHeader(gcib, EncodeConstants.OF13_VERSION_ID);
        GetConfigInput gci = gcib.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        getConfigFactory.serialize(gci, out);

        BufferHelper.checkHeaderV13(out, GET_CONFIG_REQUEST_MESSAGE_CODE_TYPE, 8);
    }

    /**
     * Testing of {@link GetConfigInputMessageFactory} for correct translation from POJO
     * @throws Exception
     */
    @Test
    public void testV10() throws Exception {
        GetConfigInputBuilder gcib = new GetConfigInputBuilder();
        BufferHelper.setupHeader(gcib, EncodeConstants.OF10_VERSION_ID);
        GetConfigInput gci = gcib.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        getConfigFactory.serialize(gci, out);

        BufferHelper.checkHeaderV10(out, GET_CONFIG_REQUEST_MESSAGE_CODE_TYPE, 8);
    }

}
