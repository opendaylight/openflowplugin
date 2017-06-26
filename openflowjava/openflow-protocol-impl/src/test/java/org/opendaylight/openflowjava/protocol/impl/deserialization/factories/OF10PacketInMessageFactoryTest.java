/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
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
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;

/**
 * @author michal.polkorab
 */
public class OF10PacketInMessageFactoryTest {

    private OFDeserializer<PacketInMessage> packetInFactory;

    /**
     * Initializes deserializer registry and lookups correct deserializer
     */
    @Before
    public void startUp() {
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        packetInFactory = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 10, PacketInMessage.class));
    }

    /**
     * Testing {@link OF10PacketInMessageFactory} for correct translation into POJO
     */
    @Test
    public void test(){
        ByteBuf bb = BufferHelper.buildBuffer("00 01 02 03 01 02 01 02 00 00 01 02 03 04");
        PacketInMessage builtByFactory = BufferHelper.deserialize(packetInFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong bufferID", 0x00010203L, builtByFactory.getBufferId().longValue());
        Assert.assertEquals("Wrong totalLength", 0x0102, builtByFactory.getTotalLen().intValue());
        Assert.assertEquals("Wrong inPort", 0x0102, builtByFactory.getInPort().intValue());
        Assert.assertEquals("Wrong reason", PacketInReason.OFPRNOMATCH, builtByFactory.getReason());
        Assert.assertArrayEquals("Wrong data", ByteBufUtils.hexStringToBytes("01 02 03 04"), builtByFactory.getData());
    }

    /**
     * Testing {@link OF10PacketInMessageFactory} for correct translation into POJO
     */
    @Test
    public void testWithNoAdditionalData(){
        ByteBuf bb = BufferHelper.buildBuffer("00 01 02 03 01 02 01 02 00 00");
        PacketInMessage builtByFactory = BufferHelper.deserialize(packetInFactory, bb);

        Assert.assertNull("Wrong data", builtByFactory.getData());
    }
}