/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.messages;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.AbstractDeserializerTest;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortMessage;

public class PortMessageDeserializerTest extends AbstractDeserializerTest {
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_1 = 4;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_2 = 2;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_3 = 4;

    // Port features
    private static final Boolean IS_AUTOENG = true;
    private static final Boolean IS_COPPER = false;
    private static final Boolean IS_FIBER = true;
    private static final Boolean IS_40GBFD = true;
    private static final Boolean IS_100GBFD = false;
    private static final Boolean IS_100MBFD = false;
    private static final Boolean IS_100MBHD = true;
    private static final Boolean IS_1GBFD = false;
    private static final Boolean IS_1GBHD = false;
    private static final Boolean IS_1TBFD = true;
    private static final Boolean IS_OTHER = true;
    private static final Boolean IS_PAUSE = false;
    private static final Boolean IS_PAUSE_ASYM = false;
    private static final Boolean IS_10GBFD = false;
    private static final Boolean IS_10MBFD = true;
    private static final Boolean IS_10MBHD = true;
    private static final Boolean IS_NOPACKETIN = false;
    private static final Boolean IS_NOFWD = true;
    private static final Boolean IS_NORECV = true;
    private static final Boolean IS_PORTDOWN = true;
    private static final int PORT = 22;
    private static final int XID = 42;
    private static final int TYPE = 16;
    private static final MacAddress ADDRESS = new MacAddress("00:01:02:03:04:05");

    private ByteBuf buffer;

    @Override
    protected void init() {
        buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void deserialize() throws Exception {
        buffer.writeByte(TYPE);
        buffer.writeShort(EncodeConstants.EMPTY_LENGTH);
        buffer.writeInt(XID);
        buffer.writeInt(PORT);
        buffer.writeZero(PADDING_IN_PORT_MOD_MESSAGE_1);
        buffer.writeBytes(IetfYangUtil.INSTANCE.bytesFor(ADDRESS));

        final int config = ByteBufUtils.fillBitMaskFromMap(ImmutableMap
                .<Integer, Boolean>builder()
                .put(0, IS_PORTDOWN)
                .put(2, IS_NORECV)
                .put(5, IS_NOFWD)
                .put(6, IS_NOPACKETIN)
                .build());

        buffer.writeZero(PADDING_IN_PORT_MOD_MESSAGE_2);
        buffer.writeInt(config); // config
        buffer.writeInt(config); // config mask

        buffer.writeInt(ByteBufUtils.fillBitMask(0,
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

        buffer.writeZero(PADDING_IN_PORT_MOD_MESSAGE_3);

        final PortMessage message = (PortMessage) getFactory().deserialize(buffer, EncodeConstants.OF13_VERSION_ID);

        assertEquals(XID, message.getXid().intValue());
        assertEquals(PORT, message.getPortNumber().getUint32().intValue());
        assertEquals(ADDRESS.getValue(), message.getHardwareAddress().getValue());

        // Config
        assertEquals(IS_PORTDOWN, message.getConfiguration().isPORTDOWN());
        assertEquals(IS_NORECV, message.getConfiguration().isNORECV());
        assertEquals(IS_NOFWD, message.getConfiguration().isNOFWD());
        assertEquals(IS_NOPACKETIN, message.getConfiguration().isNOPACKETIN());

        // Features
        assertEquals(IS_10MBHD, message.getAdvertisedFeatures().isTenMbHd());
        assertEquals(IS_10MBFD, message.getAdvertisedFeatures().isTenMbHd());
        assertEquals(IS_100MBHD, message.getAdvertisedFeatures().isHundredMbHd());
        assertEquals(IS_100MBFD, message.getAdvertisedFeatures().isHundredMbFd());
        assertEquals(IS_1GBHD, message.getAdvertisedFeatures().isOneGbHd());
        assertEquals(IS_1GBFD, message.getAdvertisedFeatures().isOneGbFd());
        assertEquals(IS_10GBFD, message.getAdvertisedFeatures().isTenGbFd());
        assertEquals(IS_40GBFD, message.getAdvertisedFeatures().isFortyGbFd());
        assertEquals(IS_100GBFD, message.getAdvertisedFeatures().isHundredGbFd());
        assertEquals(IS_1TBFD, message.getAdvertisedFeatures().isOneTbFd());
        assertEquals(IS_OTHER, message.getAdvertisedFeatures().isOther());
        assertEquals(IS_COPPER, message.getAdvertisedFeatures().isCopper());
        assertEquals(IS_FIBER, message.getAdvertisedFeatures().isFiber());
        assertEquals(IS_AUTOENG, message.getAdvertisedFeatures().isAutoeng());
        assertEquals(IS_PAUSE, message.getAdvertisedFeatures().isPause());
        assertEquals(IS_PAUSE_ASYM, message.getAdvertisedFeatures().isPauseAsym());

        assertEquals(buffer.readableBytes(), 0);
    }

}
