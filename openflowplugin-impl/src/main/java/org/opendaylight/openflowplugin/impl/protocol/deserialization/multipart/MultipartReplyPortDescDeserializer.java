/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.State;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.multipart.reply.multipart.reply.body.MultipartReplyPortDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.multipart.reply.multipart.reply.body.multipart.reply.port.desc.Ports;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.multipart.reply.multipart.reply.body.multipart.reply.port.desc.PortsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;

public class MultipartReplyPortDescDeserializer implements OFDeserializer<MultipartReplyBody> {

    private static final byte PADDING_IN_PORT_DESC_HEADER_01 = 4;
    private static final byte PADDING_IN_PORT_DESC_HEADER_02 = 2;

    @Override
    public MultipartReplyBody deserialize(ByteBuf message) {
        final MultipartReplyPortDescBuilder builder = new MultipartReplyPortDescBuilder();
        final List<Ports> items = new ArrayList<>();

        while (message.readableBytes() > 0) {
            final PortsBuilder itemBuilder = new PortsBuilder();
            itemBuilder.setPortNumber(new PortNumberUni(message.readUnsignedInt()));
            message.skipBytes(PADDING_IN_PORT_DESC_HEADER_01);
            itemBuilder.setHardwareAddress(ByteBufUtils.readIetfMacAddress(message));
            message.skipBytes(PADDING_IN_PORT_DESC_HEADER_02);

            items.add(itemBuilder
                .setName(ByteBufUtils.decodeNullTerminatedString(message, EncodeConstants.MAX_PORT_NAME_LENGTH))
                .setConfiguration(readPortConfig(message))
                .setState(readPortState(message))
                .setCurrentFeature(readPortFeatures(message))
                .setAdvertisedFeatures(readPortFeatures(message))
                .setSupported(readPortFeatures(message))
                .setPeerFeatures(readPortFeatures(message))
                .setCurrentSpeed(message.readUnsignedInt())
                .setMaximumSpeed(message.readUnsignedInt())
                .build());
        }

        return builder
            .setPorts(items)
            .build();
    }


    private static PortConfig readPortConfig(final ByteBuf message) {
        final long input = message.readUnsignedInt();
        final Boolean pcPortDown = ((input) & (1)) != 0;
        final Boolean pcNRecv = ((input) & (1 << 2)) != 0;
        final Boolean pcNFwd = ((input) & (1 << 5)) != 0;
        final Boolean pcNPacketIn = ((input) & (1 << 6)) != 0;
        return new PortConfig(pcNFwd, pcNPacketIn, pcNRecv, pcPortDown);
    }

    private static State readPortState(final ByteBuf message) {
        final long input = message.readUnsignedInt();
        final Boolean psLinkDown = ((input) & (1)) != 0;
        final Boolean psBlocked = ((input) & (1 << 1)) != 0;
        final Boolean psLive = ((input) & (1 << 2)) != 0;
        return new StateBuilder()
            .setBlocked(psBlocked)
            .setLinkDown(psLinkDown)
            .setLive(psLive)
            .build();
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
        return new PortFeatures(pfAutoneg, pfCopper, pfFiber, pf40gbFd,
            pf100gbFd, pf100mbFd, pf100mbHd, pf1gbFd, pf1gbHd, pf1tbFd,
            pfOther, pfPause, pfPauseAsym, pf10gbFd, pf10mbFd, pf10mbHd);
    }
}
