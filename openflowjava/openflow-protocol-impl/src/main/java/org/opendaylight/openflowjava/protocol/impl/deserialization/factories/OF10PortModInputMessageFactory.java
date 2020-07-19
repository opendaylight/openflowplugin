/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInputBuilder;

/**
 * Translates PortModInput messages.
 *
 * @author giuseppex.petralia@intel.com
 */
public class OF10PortModInputMessageFactory implements OFDeserializer<PortModInput> {

    @Override
    public PortModInput deserialize(final ByteBuf rawMessage) {
        return new PortModInputBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_0)
                .setXid(readUint32(rawMessage))
                .setPortNo(new PortNumber(readUint16(rawMessage).toUint32()))
                .setHwAddress(ByteBufUtils.readIetfMacAddress(rawMessage))
                .setConfigV10(createPortConfig(rawMessage.readUnsignedInt()))
                .setMaskV10(createPortConfig(rawMessage.readUnsignedInt()))
                .setAdvertiseV10(createPortFeatures(rawMessage.readUnsignedInt()))
                .build();
    }

    private static PortConfigV10 createPortConfig(final long input) {
        final Boolean _portDown = (input & 1 << 0) != 0;
        final Boolean _noStp = (input & 1 << 1) != 0;
        final Boolean _noRecv = (input & 1 << 2) != 0;
        final Boolean _noRecvStp = (input & 1 << 3) != 0;
        final Boolean _noFlood = (input & 1 << 4) != 0;
        final Boolean _noFwd = (input & 1 << 5) != 0;
        final Boolean _noPacketIn = (input & 1 << 6) != 0;
        return new PortConfigV10(_noFlood, _noFwd, _noPacketIn, _noRecv, _noRecvStp, _noStp, _portDown);
    }

    private static PortFeaturesV10 createPortFeatures(final long input) {
        final Boolean _10mbHd = (input & 1 << 0) != 0;
        final Boolean _10mbFd = (input & 1 << 1) != 0;
        final Boolean _100mbHd = (input & 1 << 2) != 0;
        final Boolean _100mbFd = (input & 1 << 3) != 0;
        final Boolean _1gbHd = (input & 1 << 4) != 0;
        final Boolean _1gbFd = (input & 1 << 5) != 0;
        final Boolean _10gbFd = (input & 1 << 6) != 0;
        final Boolean _copper = (input & 1 << 7) != 0;
        final Boolean _fiber = (input & 1 << 8) != 0;
        final Boolean _autoneg = (input & 1 << 9) != 0;
        final Boolean _pause = (input & 1 << 10) != 0;
        final Boolean _pauseAsym = (input & 1 << 11) != 0;
        return new PortFeaturesV10(_100mbFd, _100mbHd, _10gbFd, _10mbFd, _10mbHd, _1gbFd, _1gbHd, _autoneg, _copper,
                _fiber, _pause, _pauseAsym);
    }

}
