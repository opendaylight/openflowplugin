/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigInput;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * UNit tests for GetQueueConfigInputMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class GetQueueConfigInputMessageFactoryTest {
    private OFDeserializer<GetQueueConfigInput> factory;

    @Before
    public void startUp() {
        DeserializerRegistry desRegistry = new DeserializerRegistryImpl();
        desRegistry.init();
        factory = desRegistry
                .getDeserializer(new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 22, GetQueueConfigInput.class));
    }

    @Test
    public void test() {
        ByteBuf bb = BufferHelper.buildBuffer("00 01 02 03 00 00 00 00");
        GetQueueConfigInput deserializedMessage = BufferHelper.deserialize(factory, bb);
        BufferHelper.checkHeaderV13(deserializedMessage);
        Assert.assertEquals("Wrong  Port No", new PortNumber(Uint32.valueOf(0x00010203)),
            deserializedMessage.getPort());
    }

}
