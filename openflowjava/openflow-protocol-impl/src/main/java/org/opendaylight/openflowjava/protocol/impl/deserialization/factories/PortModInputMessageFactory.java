/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInputBuilder;

/**
 * Translates PortModInput messages.
 *
 * @author giuseppex.petralia@intel.com
 */
public class PortModInputMessageFactory implements OFDeserializer<PortModInput> {

    private static final byte PADDING_IN_PORT_MOD_MESSAGE_1 = 4;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_2 = 2;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_3 = 4;

    @Override
    public PortModInput deserialize(final ByteBuf rawMessage) {
        PortModInputBuilder builder = new PortModInputBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .setXid(readUint32(rawMessage))
                .setPortNo(new PortNumber(readUint32(rawMessage)));
        rawMessage.skipBytes(PADDING_IN_PORT_MOD_MESSAGE_1);
        builder.setHwAddress(ByteBufUtils.readIetfMacAddress(rawMessage));
        rawMessage.skipBytes(PADDING_IN_PORT_MOD_MESSAGE_2);
        builder.setConfig(createPortConfig(rawMessage.readUnsignedInt()));
        builder.setMask(createPortConfig(rawMessage.readUnsignedInt()));
        builder.setAdvertise(createPortFeatures(rawMessage.readUnsignedInt()));
        rawMessage.skipBytes(PADDING_IN_PORT_MOD_MESSAGE_3);
        return builder.build();
    }

    private static PortConfig createPortConfig(final long input) {
        final Boolean pcPortDown = (input & 1 << 0) != 0;
        final Boolean pcNRecv = (input & 1 << 2) != 0;
        final Boolean pcNFwd = (input & 1 << 5) != 0;
        final Boolean pcNPacketIn = (input & 1 << 6) != 0;
        return new PortConfig(pcNFwd, pcNPacketIn, pcNRecv, pcPortDown);
    }

    private static PortFeatures createPortFeatures(final long input) {
        final Boolean pf10mbHd = (input & 1 << 0) != 0;
        final Boolean pf10mbFd = (input & 1 << 1) != 0;
        final Boolean pf100mbHd = (input & 1 << 2) != 0;
        final Boolean pf100mbFd = (input & 1 << 3) != 0;
        final Boolean pf1gbHd = (input & 1 << 4) != 0;
        final Boolean pf1gbFd = (input & 1 << 5) != 0;
        final Boolean pf10gbFd = (input & 1 << 6) != 0;
        final Boolean pf40gbFd = (input & 1 << 7) != 0;
        final Boolean pf100gbFd = (input & 1 << 8) != 0;
        final Boolean pf1tbFd = (input & 1 << 9) != 0;
        final Boolean pfOther = (input & 1 << 10) != 0;
        final Boolean pfCopper = (input & 1 << 11) != 0;
        final Boolean pfFiber = (input & 1 << 12) != 0;
        final Boolean pfAutoneg = (input & 1 << 13) != 0;
        final Boolean pfPause = (input & 1 << 14) != 0;
        final Boolean pfPauseAsym = (input & 1 << 15) != 0;
        return new PortFeatures(pf100gbFd, pf100mbFd, pf100mbHd, pf10gbFd, pf10mbFd, pf10mbHd, pf1gbFd, pf1gbHd,
                pf1tbFd, pf40gbFd, pfAutoneg, pfCopper, pfFiber, pfOther, pfPause, pfPauseAsym);
    }
}
