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
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for FlowRemovedMessageFactory.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class FlowRemovedMessageFactoryTest {

    private OFDeserializer<FlowRemovedMessage> flowFactory;

    /**
     * Initializes deserializer registry and lookups correct deserializer.
     */
    @Before
    public void startUp() {
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        flowFactory = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 11, FlowRemovedMessage.class));
    }

    /**
     * Testing {@link FlowRemovedMessageFactory} for correct translation into POJO.
     */
    @Test
    public void test() {
        ByteBuf bb = BufferHelper.buildBuffer("00 01 02 03 04 05 06 07 00 03 02 04 00 00 00 02"
                + " 00 00 00 05 00 01 00 03 00 01 02 03 04 05 06 07 00 01 02 03 04 05 06 07");
        FlowRemovedMessage builtByFactory = BufferHelper.deserialize(flowFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);

        Assert.assertTrue(builtByFactory.getCookie().longValue() == 0x0001020304050607L);
        Assert.assertTrue(builtByFactory.getPriority().toJava() == 0x03);
        Assert.assertEquals("Wrong reason", 0x02, builtByFactory.getReason().getIntValue());
        Assert.assertEquals("Wrong tableId", new TableId(Uint32.valueOf(4)), builtByFactory.getTableId());
        Assert.assertEquals("Wrong durationSec", 0x02L, builtByFactory.getDurationSec().longValue());
        Assert.assertEquals("Wrong durationNsec", 0x05L, builtByFactory.getDurationNsec().longValue());
        Assert.assertEquals("Wrong idleTimeout", 0x01, builtByFactory.getIdleTimeout().intValue());
        Assert.assertEquals("Wrong hardTimeout", 0x03, builtByFactory.getHardTimeout().intValue());
        Assert.assertEquals("Wrong packetCount", 0x0001020304050607L, builtByFactory.getPacketCount().longValue());
        Assert.assertEquals("Wrong byteCount", 0x0001020304050607L, builtByFactory.getByteCount().longValue());
    }
}
