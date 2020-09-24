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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.SwitchConfigFlag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigOutputBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

/**
 * Unit tests for GetConfigReplyMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class GetConfigReplyMessageFactoryTest {
    private static final byte MESSAGE_TYPE = 8;
    private OFSerializer<GetConfigOutput> factory;

    @Before
    public void startUp() {
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        factory = registry.getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, GetConfigOutput.class));
    }

    @Test
    public void testSerialize() throws Exception {
        GetConfigOutputBuilder builder = new GetConfigOutputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setFlags(SwitchConfigFlag.forValue(2));
        builder.setMissSendLen(Uint16.valueOf(20));
        GetConfigOutput message = builder.build();

        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);

        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 12);
        Assert.assertEquals("Wrong Type", message.getFlags().getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong Code", message.getMissSendLen().intValue(), serializedBuffer.readShort());
    }
}
