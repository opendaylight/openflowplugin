/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
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
 * @author giuseppex.petralia@intel.com
 *
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
        outBuffer.writeBytes(IetfYangUtil.INSTANCE.bytesFor(message.getHwAddr()));
        writeName(message.getName(), outBuffer);
        writePortConfig(message.getConfigV10(), outBuffer);
        writePortState(message.getStateV10(), outBuffer);
        writePortFeature(message.getCurrentFeaturesV10(), outBuffer);
        writePortFeature(message.getAdvertisedFeaturesV10(), outBuffer);
        writePortFeature(message.getSupportedFeaturesV10(), outBuffer);
        writePortFeature(message.getPeerFeaturesV10(), outBuffer);
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    private void writePortFeature(final PortFeaturesV10 feature, final ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, feature.is_10mbHd());
        map.put(1, feature.is_10mbFd());
        map.put(2, feature.is_100mbHd());
        map.put(3, feature.is_100mbFd());
        map.put(4, feature.is_1gbHd());
        map.put(5, feature.is_1gbFd());
        map.put(6, feature.is_10gbFd());
        map.put(7, feature.isCopper());
        map.put(8, feature.isFiber());
        map.put(9, feature.isAutoneg());
        map.put(10, feature.isPause());
        map.put(11, feature.isPauseAsym());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }

    private void writePortState(final PortStateV10 state, final ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, state.isLinkDown());
        map.put(1, state.isBlocked());
        map.put(2, state.isLive());
        map.put(3, state.isStpListen());
        map.put(4, state.isStpLearn());
        map.put(5, state.isStpForward());
        map.put(6, state.isStpBlock());
        map.put(7, state.isStpMask());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }

    private void writePortConfig(final PortConfigV10 config, final ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, config.isPortDown());
        map.put(1, config.isNoStp());
        map.put(2, config.isNoRecv());
        map.put(3, config.isNoRecvStp());
        map.put(4, config.isNoFlood());
        map.put(5, config.isNoFwd());
        map.put(6, config.isNoPacketIn());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }

    private void writeName(final String name, final ByteBuf outBuffer) {
        byte[] nameBytes = name.getBytes();
        if (nameBytes.length < 16) {
            byte[] nameBytesPadding = new byte[16];
            int i = 0;
            for (byte b : nameBytes) {
                nameBytesPadding[i] = b;
                i++;
            }
            for (; i < 16; i++) {
                nameBytesPadding[i] = 0x0;
            }
            outBuffer.writeBytes(nameBytesPadding);
        } else {
            outBuffer.writeBytes(nameBytes);
        }
    }
}
