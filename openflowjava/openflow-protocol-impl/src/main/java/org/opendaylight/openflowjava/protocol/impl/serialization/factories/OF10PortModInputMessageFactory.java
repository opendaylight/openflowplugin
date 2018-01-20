/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;

/**
 * Translates PortMod messages.
 *
 * @author michal.polkorab
 */
public class OF10PortModInputMessageFactory implements OFSerializer<PortModInput> {

    private static final byte MESSAGE_TYPE = 15;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE = 4;

    @Override
    public void serialize(final PortModInput message, final ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeShort(message.getPortNo().getValue().intValue());
        outBuffer.writeBytes(IetfYangUtil.INSTANCE.bytesFor(message.getHwAddress()));
        outBuffer.writeInt(createPortConfigBitmask(message.getConfigV10()));
        outBuffer.writeInt(createPortConfigBitmask(message.getMaskV10()));
        outBuffer.writeInt(createPortFeaturesBitmask(message.getAdvertiseV10()));
        outBuffer.writeZero(PADDING_IN_PORT_MOD_MESSAGE);
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    private static int createPortConfigBitmask(final PortConfigV10 config) {
        return ByteBufUtils.fillBitMask(0,
                config.isPortDown(),
                config.isNoStp(),
                config.isNoRecv(),
                config.isNoRecvStp(),
                config.isNoFlood(),
                config.isNoFwd(),
                config.isNoPacketIn());
    }

    private static int createPortFeaturesBitmask(final PortFeaturesV10 feature) {
        return ByteBufUtils.fillBitMask(0,
                feature.is_10mbHd(),
                feature.is_10mbFd(),
                feature.is_100mbHd(),
                feature.is_100mbFd(),
                feature.is_1gbHd(),
                feature.is_1gbFd(),
                feature.is_10gbFd(),
                feature.isCopper(),
                feature.isFiber(),
                feature.isAutoneg(),
                feature.isPause(),
                feature.isPauseAsym());
    }

}
