/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;

/**
 * Translates PortStatus messages.
 *
 * @author giuseppex.petralia@intel.com
 */
public class PortStatusMessageFactory implements OFSerializer<PortStatusMessage> {

    private static final byte MESSAGE_TYPE = 12;
    private static final byte PADDING = 7;
    private static final byte PORT_PADDING_1 = 4;
    private static final byte PORT_PADDING_2 = 2;

    @Override
    public void serialize(final PortStatusMessage message, final ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeByte(message.getReason().getIntValue());
        outBuffer.writeZero(PADDING);
        outBuffer.writeInt(message.getPortNo().intValue());
        outBuffer.writeZero(PORT_PADDING_1);
        outBuffer.writeBytes(IetfYangUtil.macAddressBytes(message.getHwAddr()));
        outBuffer.writeZero(PORT_PADDING_2);
        writeName(message.getName(), outBuffer);
        writePortConfig(message.getConfig(), outBuffer);
        writePortState(message.getState(), outBuffer);
        writePortFeatures(message.getCurrentFeatures(), outBuffer);
        writePortFeatures(message.getAdvertisedFeatures(), outBuffer);
        writePortFeatures(message.getSupportedFeatures(), outBuffer);
        writePortFeatures(message.getPeerFeatures(), outBuffer);
        outBuffer.writeInt(message.getCurrSpeed().intValue());
        outBuffer.writeInt(message.getMaxSpeed().intValue());
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    private static void writePortConfig(final PortConfig config, final ByteBuf outBuffer) {
        outBuffer.writeInt(ByteBufUtils.bitOf(0, config.getPortDown())
            | ByteBufUtils.bitOf(2, config.getNoRecv())
            | ByteBufUtils.bitOf(5, config.getNoFwd())
            | ByteBufUtils.bitOf(6, config.getNoPacketIn()));
    }

    private static void writeName(final String name, final ByteBuf outBuffer) {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        if (nameBytes.length < 16) {
            byte[] nameBytesPadding = new byte[16];
            int index = 0;
            for (byte b : nameBytes) {
                nameBytesPadding[index] = b;
                index++;
            }
            for (; index < 16; index++) {
                nameBytesPadding[index] = 0x0;
            }
            outBuffer.writeBytes(nameBytesPadding);
        } else {
            outBuffer.writeBytes(nameBytes);
        }
    }

    private static void writePortState(final PortState state, final ByteBuf outBuffer) {
        outBuffer.writeInt(ByteBufUtils.fillBitMask(
            state.getLinkDown(),
            state.getBlocked(),
            state.getLive()));
    }

    private static void writePortFeatures(final PortFeatures features, final ByteBuf outBuffer) {
        outBuffer.writeInt(ByteBufUtils.fillBitMask(
            features.get_10mbHd(),
            features.get_10mbFd(),
            features.get_100mbHd(),
            features.get_100mbFd(),
            features.get_1gbHd(),
            features.get_1gbFd(),
            features.get_10gbFd(),
            features.get_40gbFd(),
            features.get_100gbFd(),
            features.get_1tbFd(),
            features.getOther(),
            features.getCopper(),
            features.getFiber(),
            features.getAutoneg(),
            features.getPause(),
            features.getPauseAsym()));
    }
}
