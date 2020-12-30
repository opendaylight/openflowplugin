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
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortStateV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;

/**
 * Translates PortStatus messages.
 *
 * @author giuseppex.petralia@intel.com
 */
public class OF10PortStatusMessageFactory implements OFSerializer<PortStatusMessage> {

    private static final byte MESSAGE_TYPE = 12;
    private static final byte PADDING = 7;

    @Override
    public void serialize(final PortStatusMessage message, final ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeByte(message.getReason().getIntValue());
        outBuffer.writeZero(PADDING);
        outBuffer.writeShort(message.getPortNo().intValue());
        outBuffer.writeBytes(IetfYangUtil.INSTANCE.macAddressBytes(message.getHwAddr()));
        writeName(message.getName(), outBuffer);
        writePortConfig(message.getConfigV10(), outBuffer);
        writePortState(message.getStateV10(), outBuffer);
        writePortFeature(message.getCurrentFeaturesV10(), outBuffer);
        writePortFeature(message.getAdvertisedFeaturesV10(), outBuffer);
        writePortFeature(message.getSupportedFeaturesV10(), outBuffer);
        writePortFeature(message.getPeerFeaturesV10(), outBuffer);
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    private static void writePortFeature(final PortFeaturesV10 feature, final ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, feature.get_10mbHd());
        map.put(1, feature.get_10mbFd());
        map.put(2, feature.get_100mbHd());
        map.put(3, feature.get_100mbFd());
        map.put(4, feature.get_1gbHd());
        map.put(5, feature.get_1gbFd());
        map.put(6, feature.get_10gbFd());
        map.put(7, feature.getCopper());
        map.put(8, feature.getFiber());
        map.put(9, feature.getAutoneg());
        map.put(10, feature.getPause());
        map.put(11, feature.getPauseAsym());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }

    private static void writePortState(final PortStateV10 state, final ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, state.getLinkDown());
        map.put(1, state.getBlocked());
        map.put(2, state.getLive());
        map.put(3, state.getStpListen());
        map.put(4, state.getStpLearn());
        map.put(5, state.getStpForward());
        map.put(6, state.getStpBlock());
        map.put(7, state.getStpMask());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }

    private static void writePortConfig(final PortConfigV10 config, final ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, config.getPortDown());
        map.put(1, config.getNoStp());
        map.put(2, config.getNoRecv());
        map.put(3, config.getNoRecvStp());
        map.put(4, config.getNoFlood());
        map.put(5, config.getNoFwd());
        map.put(6, config.getNoPacketIn());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
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
}
