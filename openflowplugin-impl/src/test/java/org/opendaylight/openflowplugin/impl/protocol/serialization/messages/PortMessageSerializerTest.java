/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.impl.protocol.serialization.AbstractSerializerTest;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumberValues;

public class PortMessageSerializerTest extends AbstractSerializerTest {

    private static final byte PADDING_IN_PORT_MOD_MESSAGE_01 = 4;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_02 = 2;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_03 = 4;
    private static final short LENGTH = 40;
    private static final Long XID = 42L;
    private static final short VERSION = EncodeConstants.OF13_VERSION_ID;
    private static final String PORT_NUMBER = OutputPortValues.ALL.toString();
    private static final Long PORT_NUMBER_VAL = BinContent.intToUnsignedLong(PortNumberValues.ALL.getIntValue());
    private static final String MAC_ADDRESS = "E9:2A:55:BA:FA:4D";

    // Port config
    private static final Boolean IS_NOFWD = false;
    private static final Boolean IS_NOPACKETIN = false;
    private static final Boolean IS_NORECV = true;
    private static final Boolean IS_PORTDOWN = false;

    // Port features
    private static final Boolean IS_AUTOENG = true;
    private static final Boolean IS_COPPER = false;
    private static final Boolean IS_FIBER = true;
    private static final Boolean IS_40GBFD = true;
    private static final Boolean IS_100GBFD = false;
    private static final Boolean IS_100MBFD = false;
    private static final Boolean IS_100MBHD = false;
    private static final Boolean IS_1GBFD = false;
    private static final Boolean IS_1GBHD = false;
    private static final Boolean IS_1TBFD = false;
    private static final Boolean IS_OTHER = false;
    private static final Boolean IS_PAUSE = false;
    private static final Boolean IS_PAUSE_ASYM = false;
    private static final Boolean IS_10GBFD = false;
    private static final Boolean IS_10MBFD = false;
    private static final Boolean IS_10MBHD = false;

    private static final PortMessage MESSAGE = new PortMessageBuilder()
            .setXid(XID)
            .setVersion(VERSION)
            .setPortNumber(new PortNumberUni(PORT_NUMBER))
            .setConfiguration(new PortConfig(IS_NOFWD, IS_NOPACKETIN, IS_NORECV, IS_PORTDOWN))
            .setMask(new PortConfig(true, true, true, true))
            .setAdvertisedFeatures(new PortFeatures(
                    IS_AUTOENG,
                    IS_COPPER,
                    IS_FIBER,
                    IS_40GBFD,
                    IS_100GBFD,
                    IS_100MBFD,
                    IS_100MBHD,
                    IS_1GBFD,
                    IS_1GBHD,
                    IS_1TBFD,
                    IS_OTHER,
                    IS_PAUSE,
                    IS_PAUSE_ASYM,
                    IS_10GBFD,
                    IS_10MBFD,
                    IS_10MBHD))
            .setHardwareAddress(new MacAddress(MAC_ADDRESS))
            .build();

    private PortMessageSerializer serializer;

    @Override
    protected void init() {
        serializer = getRegistry().getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, PortMessage.class));
    }

    @Test
    public void testSerialize() throws Exception {
        final ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(MESSAGE, out);

        // Header
        assertEquals(out.readByte(), VERSION);
        assertEquals(out.readByte(), serializer.getMessageType());
        assertEquals(out.readShort(), LENGTH);
        assertEquals(out.readInt(), XID.intValue());

        // Body
        assertEquals(out.readInt(), PORT_NUMBER_VAL.intValue());
        out.skipBytes(PADDING_IN_PORT_MOD_MESSAGE_01);
        byte[] address = new byte[6];
        out.readBytes(address);
        assertEquals(
                new MacAddress(ByteBufUtils.macAddressToString(address)).getValue(),
                new MacAddress(MAC_ADDRESS).getValue());
        out.skipBytes(PADDING_IN_PORT_MOD_MESSAGE_02);

        // Port config
        final int config = ByteBufUtils.fillBitMaskFromMap(ImmutableMap
                .<Integer, Boolean>builder()
                .put(0, IS_PORTDOWN)
                .put(2, IS_NORECV)
                .put(5, IS_NOFWD)
                .put(6, IS_NOPACKETIN)
                .build());

        final int mask = ByteBufUtils.fillBitMaskFromMap(ImmutableMap
                .<Integer, Boolean>builder()
                .put(0, true)
                .put(2, true)
                .put(5, true)
                .put(6, true)
                .build());

        assertEquals(out.readInt(), config);
        assertEquals(out.readInt(), mask);

        // Port features
        assertEquals(out.readInt(), ByteBufUtils.fillBitMask(0,
                IS_10MBHD,
                IS_10MBFD,
                IS_100MBHD,
                IS_100MBFD,
                IS_1GBHD,
                IS_1GBFD,
                IS_10GBFD,
                IS_40GBFD,
                IS_100GBFD,
                IS_1TBFD,
                IS_OTHER,
                IS_COPPER,
                IS_FIBER,
                IS_AUTOENG,
                IS_PAUSE,
                IS_PAUSE_ASYM));

        out.skipBytes(PADDING_IN_PORT_MOD_MESSAGE_03);

        assertEquals(out.readableBytes(), 0);

    }

}