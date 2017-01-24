/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.multipart.reply.multipart.reply.body.MultipartReplyPortDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;

public class MultipartReplyPortDescDeserializerTest extends AbstractMultipartDeserializerTest{
    private static final byte PADDING_IN_PORT_DESC_HEADER_01 = 4;
    private static final byte PADDING_IN_PORT_DESC_HEADER_02 = 2;

    private static final int PORT_NUMBER = 1;
    private static final byte[] MAC_ADDRESS = new byte[] {1,2,3,4,5,6};

    private static final boolean PC_PORT_DOWN = true;
    private static final boolean PC_N0_RECV = true;
    private static final boolean PC_N0_FWD = false;
    private static final boolean PC_N0_PACKET_IN = true;
    private static final PortConfig PORT_CONFIG = new PortConfig(PC_N0_FWD, PC_N0_PACKET_IN, PC_N0_RECV, PC_PORT_DOWN);

    private static final boolean PS_LINK_DOWN = true;
    private static final boolean PS_BLOCKED = true;
    private static final boolean PS_LIVE = false;

    private static final boolean PF10MBHD = true;
    private static final boolean PF10MBFD = true;
    private static final boolean PF100MBHD = true;
    private static final boolean PF100MBFD = true;
    private static final boolean PF1GBHD = true;
    private static final boolean PF1GBFD = true;
    private static final boolean PF10GBFD = true;
    private static final boolean PF40GBFD = true;
    private static final boolean PF100GBFD = true;
    private static final boolean PF1TBFD = false;
    private static final boolean PFOTHER = false;
    private static final boolean PFCOPPER = false;
    private static final boolean PFFIBER = false;
    private static final boolean PFAUTONEG = false;
    private static final boolean PFPAUSE = false;
    private static final boolean PFPAUSEASYM = false;
    private static  final PortFeatures CURRENT_FEATURE = new PortFeatures(PFAUTONEG, PFCOPPER, PFFIBER, PF40GBFD,
                                                        PF100GBFD, PF100MBFD, PF100MBHD, PF1GBFD, PF1GBHD, PF1TBFD,
                                                        PFOTHER, PFPAUSE, PFPAUSEASYM, PF10GBFD, PF10MBFD, PF10MBHD);
    private static final int ADVERTISED_FEATURE = 6;
    private static final int SUPPORTED_FEATURE = 7;
    private static final int PEER_FEATURES = 8;
    private static final int CURRENT_SPEED = 9;
    private static final int MAXIMUM_SPEED = 10;

    @Test
    public void deserialize() throws Exception {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeInt(PORT_NUMBER);
        buffer.writeZero(PADDING_IN_PORT_DESC_HEADER_01);
        buffer.writeBytes(MAC_ADDRESS);
        buffer.writeZero(PADDING_IN_PORT_DESC_HEADER_02);
        buffer.writeZero(EncodeConstants.MAX_PORT_NAME_LENGTH);

        Map<Integer, Boolean> portMap = new HashMap<>();
        portMap.put(new Integer(0), PORT_CONFIG.isPORTDOWN());
        portMap.put(new Integer(2), PORT_CONFIG.isNORECV());
        portMap.put(new Integer(5), PORT_CONFIG.isNOFWD());
        portMap.put(new Integer(6), PORT_CONFIG.isNOPACKETIN());

        buffer.writeInt(ByteBufUtils.fillBitMaskFromMap(portMap));
        buffer.writeInt(ByteBufUtils.fillBitMask(0, PS_BLOCKED, PS_LINK_DOWN, PS_LIVE));
        buffer.writeInt(ByteBufUtils.fillBitMask(0,
                CURRENT_FEATURE.isTenMbHd(),
                CURRENT_FEATURE.isTenMbFd(),
                CURRENT_FEATURE.isHundredMbHd(),
                CURRENT_FEATURE.isHundredMbFd(),
                CURRENT_FEATURE.isOneGbHd(),
                CURRENT_FEATURE.isOneGbFd(),
                CURRENT_FEATURE.isTenGbFd(),
                CURRENT_FEATURE.isFortyGbFd(),
                CURRENT_FEATURE.isHundredGbFd(),
                CURRENT_FEATURE.isOneTbFd(),
                CURRENT_FEATURE.isOther(),
                CURRENT_FEATURE.isFiber(),
                CURRENT_FEATURE.isAutoeng(),
                CURRENT_FEATURE.isCopper(),
                CURRENT_FEATURE.isPause(),
                CURRENT_FEATURE.isPauseAsym()));
        buffer.writeInt(ADVERTISED_FEATURE);
        buffer.writeInt(SUPPORTED_FEATURE);
        buffer.writeInt(PEER_FEATURES);
        buffer.writeInt(CURRENT_SPEED);
        buffer.writeInt(MAXIMUM_SPEED);

        final MultipartReplyPortDesc reply = (MultipartReplyPortDesc) deserializeMultipart(buffer);
        assertEquals(PORT_NUMBER, reply.getPorts().get(0).getPortNumber().getUint32().intValue());
        assertEquals("01:02:03:04:05:06", reply.getPorts().get(0).getHardwareAddress().getValue());
        assertEquals(PORT_CONFIG, reply.getPorts().get(0).getConfiguration());
        assertEquals(PS_BLOCKED, reply.getPorts().get(0).getState().isBlocked());
        assertEquals(PS_LINK_DOWN, reply.getPorts().get(0).getState().isLinkDown());
        assertEquals(PS_LIVE, reply.getPorts().get(0).getState().isLive());
        assertEquals(CURRENT_FEATURE, reply.getPorts().get(0).getCurrentFeature());
        assertEquals(CURRENT_SPEED, reply.getPorts().get(0).getCurrentSpeed().intValue());
        assertEquals(MAXIMUM_SPEED, reply.getPorts().get(0).getMaximumSpeed().intValue());
    }

    @Override
    protected int getType() {
        return MultipartType.OFPMPPORTDESC.getIntValue();
    }
}