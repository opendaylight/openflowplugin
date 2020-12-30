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
        outBuffer.writeBytes(IetfYangUtil.INSTANCE.macAddressBytes(message.getHwAddr()));
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
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, config.getPortDown());
        map.put(2, config.getNoRecv());
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

    private static void writePortState(final PortState state, final ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, state.getLinkDown());
        map.put(1, state.getBlocked());
        map.put(2, state.getLive());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }

    private static void writePortFeatures(final PortFeatures features, final ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, features.get_10mbHd());
        map.put(1, features.get_10mbFd());
        map.put(2, features.get_100mbHd());
        map.put(3, features.get_100mbFd());
        map.put(4, features.get_1gbHd());
        map.put(5, features.get_1gbFd());
        map.put(6, features.get_10gbFd());
        map.put(7, features.get_40gbFd());
        map.put(8, features.get_100gbFd());
        map.put(9, features.get_1tbFd());
        map.put(10, features.getOther());
        map.put(11, features.getCopper());
        map.put(12, features.getFiber());
        map.put(13, features.getAutoneg());
        map.put(14, features.getPause());
        map.put(15, features.getPauseAsym());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }
}
