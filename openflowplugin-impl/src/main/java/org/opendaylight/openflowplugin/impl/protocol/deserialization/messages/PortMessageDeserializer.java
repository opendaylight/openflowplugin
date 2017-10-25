/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.messages;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;

public class PortMessageDeserializer implements OFDeserializer<PortMessage> {

    private static final byte PADDING_IN_PORT_MOD_MESSAGE_1 = 4;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_2 = 2;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_3 = 4;

    @Override
    public PortMessage deserialize(ByteBuf message) {
        final PortMessageBuilder builder = new PortMessageBuilder()
                .setVersion((short) EncodeConstants.OF13_VERSION_ID)
                .setXid(message.readUnsignedInt())
                .setPortNumber(new PortNumberUni(message.readUnsignedInt()));

        message.skipBytes(PADDING_IN_PORT_MOD_MESSAGE_1);
        builder.setHardwareAddress(ByteBufUtils.readIetfMacAddress(message));
        message.skipBytes(PADDING_IN_PORT_MOD_MESSAGE_2);
        builder.setConfiguration(readPortConfig(message));
        message.skipBytes(EncodeConstants.SIZE_OF_INT_IN_BYTES); // Skip mask
        builder.setAdvertisedFeatures(readPortFeatures(message));
        message.skipBytes(PADDING_IN_PORT_MOD_MESSAGE_3);
        return builder.build();
    }

    private static PortConfig readPortConfig(ByteBuf message) {
        final long input = message.readUnsignedInt();
        final Boolean pcPortDown = ((input) & (1)) != 0;
        final Boolean pcNRecv = ((input) & (1 << 2)) != 0;
        final Boolean pcNFwd = ((input) & (1 << 5)) != 0;
        final Boolean pcNPacketIn = ((input) & (1 << 6)) != 0;
        return new PortConfig(pcNFwd, pcNPacketIn, pcNRecv, pcPortDown);
    }

    private static PortFeatures readPortFeatures(ByteBuf message) {
        final long input = message.readUnsignedInt();
        final Boolean pf10mbHd = ((input) & (1)) != 0;
        final Boolean pf10mbFd = ((input) & (1 << 1)) != 0;
        final Boolean pf100mbHd = ((input) & (1 << 2)) != 0;
        final Boolean pf100mbFd = ((input) & (1 << 3)) != 0;
        final Boolean pf1gbHd = ((input) & (1 << 4)) != 0;
        final Boolean pf1gbFd = ((input) & (1 << 5)) != 0;
        final Boolean pf10gbFd = ((input) & (1 << 6)) != 0;
        final Boolean pf40gbFd = ((input) & (1 << 7)) != 0;
        final Boolean pf100gbFd = ((input) & (1 << 8)) != 0;
        final Boolean pf1tbFd = ((input) & (1 << 9)) != 0;
        final Boolean pfOther = ((input) & (1 << 10)) != 0;
        final Boolean pfCopper = ((input) & (1 << 11)) != 0;
        final Boolean pfFiber = ((input) & (1 << 12)) != 0;
        final Boolean pfAutoneg = ((input) & (1 << 13)) != 0;
        final Boolean pfPause = ((input) & (1 << 14)) != 0;
        final Boolean pfPauseAsym = ((input) & (1 << 15)) != 0;

        return new PortFeatures(
                pfAutoneg, pfCopper, pfFiber,
                pf40gbFd, pf100gbFd, pf100mbFd,
                pf100mbHd, pf1gbFd, pf1gbHd, pf1tbFd,
                pfOther, pfPause, pfPauseAsym,
                pf10gbFd, pf10mbFd, pf10mbHd);
    }

}
