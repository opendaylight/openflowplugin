/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
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
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutputBuilder;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class OF10BarrierReplyMessageFactoryTest {
    private OFSerializer<BarrierOutput> factory;
    private static final byte MESSAGE_TYPE = 19;

    @Before
    public void startUp() {
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        factory = registry.getSerializer(new MessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, BarrierOutput.class));
    }

    @Test
    public void testSerialize() throws Exception {
        BarrierOutputBuilder builder = new BarrierOutputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        BarrierOutput message = builder.build();

        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV10(serializedBuffer, MESSAGE_TYPE, 8);
    }
}
