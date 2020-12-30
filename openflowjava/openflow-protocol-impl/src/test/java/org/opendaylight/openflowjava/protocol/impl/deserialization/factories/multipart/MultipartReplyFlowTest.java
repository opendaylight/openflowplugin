/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories.multipart;

import io.netty.buffer.ByteBuf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.MultipartReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStats;
import org.opendaylight.yangtools.yang.common.Uint64;

/**
 * Unit tests for MultipartReplyFlow.
 *
 * @author michal.polkorab
 */
public class MultipartReplyFlowTest {

    private OFDeserializer<MultipartReplyMessage> factory;

    /**
     * Initializes deserializer registry and lookups correct deserializer.
     */
    @Before
    public void startUp() {
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        factory = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 19, MultipartReplyMessage.class));
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testEmptyMultipartReplyFlowBody() {
        ByteBuf bb = BufferHelper.buildBuffer("00 01 00 01 00 00 00 00");
        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(factory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 0x01, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().getOFPMPFREQMORE());
        MultipartReplyFlowCase messageCase = (MultipartReplyFlowCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyFlow message = messageCase.getMultipartReplyFlow();
        Assert.assertEquals("Wrong flow stats size", 0, message.nonnullFlowStats().size());
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testMultipartReplyFlowBody() {
        ByteBuf bb = BufferHelper.buildBuffer("00 01 00 01 00 00 00 00 " + //
                                              // first flow stat
                                              "00 48 08 00 " + // length, tableId, padding
                                              "00 00 00 09 " + //durationSec
                                              "00 00 00 07 " + //durationNsec
                                              "00 0C 00 0E 00 0F 00 1F " + //priority, idleTimeout, hardTimeout, flags
                                              "00 00 00 00 " + //pad_02
                                              "FF 01 01 01 01 01 01 01 " + //cookie
                                              "EF 01 01 01 01 01 01 01 " + //packetCount
                                              "7F 01 01 01 01 01 01 01 " + //byteCount
                                              "00 01 00 04 00 00 00 00 " + //empty match
                                              "00 01 00 08 06 00 00 00 " + //
                                              "00 01 00 08 06 00 00 00 " + //
                                              // second flow stat
                                              "00 48 08 00 " + // length, tableId, padding
                                              "00 00 00 09 " + //durationSec
                                              "00 00 00 07 " + //durationNsec
                                              "00 0C 00 0E 00 0F 00 00 " + // priority, idleTimeout, hardTimeout, flags
                                              "00 00 00 00 " + //pad_02
                                              "FF 01 01 01 01 01 01 01 " + //cookie
                                              "EF 01 01 01 01 01 01 01 " + //packetCount
                                              "7F 01 01 01 01 01 01 01 " + //byteCount
                                              "00 01 00 04 00 00 00 00 " + //empty match
                                              "00 01 00 08 06 00 00 00 " + //
                                              "00 01 00 08 06 00 00 00");
        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(factory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 0x01, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().getOFPMPFREQMORE());
        MultipartReplyFlowCase messageCase = (MultipartReplyFlowCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyFlow message = messageCase.getMultipartReplyFlow();
        Assert.assertEquals("Wrong flow stats size", 2, message.getFlowStats().size());
        FlowStats flowStats1 = message.getFlowStats().get(0);
        Assert.assertEquals("Wrong tableId", 8, flowStats1.getTableId().intValue());
        Assert.assertEquals("Wrong durationSec", 9, flowStats1.getDurationSec().intValue());
        Assert.assertEquals("Wrong durationNsec", 7, flowStats1.getDurationNsec().intValue());
        Assert.assertEquals("Wrong priority", 12, flowStats1.getPriority().intValue());
        Assert.assertEquals("Wrong idleTimeOut", 14, flowStats1.getIdleTimeout().intValue());
        Assert.assertEquals("Wrong hardTimeOut", 15, flowStats1.getHardTimeout().intValue());
        Assert.assertEquals("Wrong flags", new FlowModFlags(true, true, true, true, true), flowStats1.getFlags());
        Assert.assertEquals("Wrong cookie", Uint64.valueOf("FF01010101010101", 16), flowStats1.getCookie());
        Assert.assertEquals("Wrong packetCount",  Uint64.valueOf("EF01010101010101", 16), flowStats1.getPacketCount());
        Assert.assertEquals("Wrong byteCount",  Uint64.valueOf("7F01010101010101", 16), flowStats1.getByteCount());
        Assert.assertEquals("Wrong match type", OxmMatchType.class, flowStats1.getMatch().getType());
        flowStats1 = message.getFlowStats().get(1);
        Assert.assertEquals("Wrong tableId", 8, flowStats1.getTableId().intValue());
        Assert.assertEquals("Wrong durationSec", 9, flowStats1.getDurationSec().intValue());
        Assert.assertEquals("Wrong durationNsec", 7, flowStats1.getDurationNsec().intValue());
        Assert.assertEquals("Wrong priority", 12, flowStats1.getPriority().intValue());
        Assert.assertEquals("Wrong idleTimeOut", 14, flowStats1.getIdleTimeout().intValue());
        Assert.assertEquals("Wrong hardTimeOut", 15, flowStats1.getHardTimeout().intValue());
        Assert.assertEquals("Wrong flags", new FlowModFlags(false, false, false, false, false), flowStats1.getFlags());
        Assert.assertEquals("Wrong cookie", Uint64.valueOf("FF01010101010101", 16), flowStats1.getCookie());
        Assert.assertEquals("Wrong packetCount", Uint64.valueOf("EF01010101010101", 16), flowStats1.getPacketCount());
        Assert.assertEquals("Wrong byteCount", Uint64.valueOf("7F01010101010101", 16), flowStats1.getByteCount());
        Assert.assertEquals("Wrong match type", OxmMatchType.class, flowStats1.getMatch().getType());
    }
}
