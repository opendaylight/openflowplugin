/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessageBuilder;

/**
 * Translates PortStatus messages.
 *
 * @author michal.polkorab
 * @author timotej.kubas
 */
public class PortStatusMessageFactory implements OFDeserializer<PortStatusMessage> {

    private static final byte PADDING_IN_PORT_STATUS_HEADER = 7;
    private static final byte PADDING_IN_OFP_PORT_HEADER_1 = 4;
    private static final byte PADDING_IN_OFP_PORT_HEADER_2 = 2;

    @Override
    public PortStatusMessage deserialize(final ByteBuf rawMessage) {
        PortStatusMessageBuilder builder = new PortStatusMessageBuilder();
        builder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        builder.setXid(readUint32(rawMessage));
        builder.setReason(PortReason.forValue(rawMessage.readUnsignedByte()));
        rawMessage.skipBytes(PADDING_IN_PORT_STATUS_HEADER);
        builder.setPortNo(readUint32(rawMessage));
        rawMessage.skipBytes(PADDING_IN_OFP_PORT_HEADER_1);
        builder.setHwAddr(ByteBufUtils.readIetfMacAddress(rawMessage));
        rawMessage.skipBytes(PADDING_IN_OFP_PORT_HEADER_2);
        builder.setName(ByteBufUtils.decodeNullTerminatedString(rawMessage, EncodeConstants.MAX_PORT_NAME_LENGTH));
        builder.setConfig(createPortConfig(rawMessage.readUnsignedInt()));
        builder.setState(createPortState(rawMessage.readUnsignedInt()));
        builder.setCurrentFeatures(createPortFeatures(rawMessage.readUnsignedInt()));
        builder.setAdvertisedFeatures(createPortFeatures(rawMessage.readUnsignedInt()));
        builder.setSupportedFeatures(createPortFeatures(rawMessage.readUnsignedInt()));
        builder.setPeerFeatures(createPortFeatures(rawMessage.readUnsignedInt()));
        builder.setCurrSpeed(readUint32(rawMessage));
        builder.setMaxSpeed(readUint32(rawMessage));
        return builder.build();
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
        return new PortFeatures(pf100gbFd, pf100mbFd, pf100mbHd, pf10gbFd, pf10mbFd, pf10mbHd, pf1gbFd,
                pf1gbHd, pf1tbFd, pf40gbFd, pfAutoneg, pfCopper, pfFiber, pfOther, pfPause, pfPauseAsym);
    }

    private static PortState createPortState(final long input) {
        final Boolean psLinkDown = (input & 1 << 0) != 0;
        final Boolean psBblocked = (input & 1 << 1) != 0;
        final Boolean psLive = (input & 1 << 2) != 0;
        return new PortState(psBblocked, psLinkDown, psLive);
    }

    private static PortConfig createPortConfig(final long input) {
        final Boolean pcPortDown = (input & 1 << 0) != 0;
        final Boolean pcNoRecv = (input & 1 << 2) != 0;
        final Boolean pcNoFwd = (input & 1 << 5) != 0;
        final Boolean pcNoPacketIn = (input & 1 << 6) != 0;
        return new PortConfig(pcNoFwd, pcNoPacketIn, pcNoRecv, pcPortDown);
    }
}
