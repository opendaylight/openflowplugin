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
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;

/**
 * @author michal.polkorab
 */
public class OF10FlowRemovedMessageFactoryTest {

    private OFDeserializer<FlowRemovedMessage> flowFactory;

    /**
     * Initializes deserializer registry and lookups correct deserializer
     */
    @Before
    public void startUp() {
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        flowFactory = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 11, FlowRemovedMessage.class));
    }

    /**
     * Testing {@link OF10FlowRemovedMessageFactory} for correct translation into POJO
     */
    @Test
    public void test(){
        ByteBuf bb = BufferHelper.buildBuffer("00 24 08 D1 00 20 AA BB CC DD EE FF "
                + "AA BB CC DD EE FF 00 05 10 00 00 08 07 06 00 00 10 11 12 13 01 02 03 04 "//36
                + "50 50 20 20 "// match
                + "00 01 02 03 04 05 06 07 00 03 01 00 00 00 00 02 "
                + "00 00 00 05 00 08 00 00 00 01 02 03 04 05 06 07 00 01 02 03 04 05 06 07");//41
        FlowRemovedMessage builtByFactory = BufferHelper.deserialize(flowFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong cookie", 0x0001020304050607L, builtByFactory.getCookie().longValue());
        Assert.assertEquals("Wrong priority", 0x03, builtByFactory.getPriority().intValue());
        Assert.assertEquals("Wrong reason", 0x01, builtByFactory.getReason().getIntValue());
        Assert.assertEquals("Wrong durationSec", 0x00000002L, builtByFactory.getDurationSec().longValue());
        Assert.assertEquals("Wrong durationNsec", 0x00000005L, builtByFactory.getDurationNsec().longValue());
        Assert.assertEquals("Wrong idleTimeout", 0x08, builtByFactory.getIdleTimeout().intValue());
        Assert.assertEquals("Wrong packetCount", 0x0001020304050607L, builtByFactory.getPacketCount().longValue());
        Assert.assertEquals("Wrong byteCount", 0x0001020304050607L, builtByFactory.getByteCount().longValue());
    }

}
