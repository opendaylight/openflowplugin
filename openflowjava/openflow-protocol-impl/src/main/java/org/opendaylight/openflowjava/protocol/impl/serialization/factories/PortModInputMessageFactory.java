/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortMod;

/**
 * Translates PortMod messages. OF protocol versions: 1.3.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class PortModInputMessageFactory implements OFSerializer<PortMod> {
    private static final byte MESSAGE_TYPE = 16;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_01 = 4;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_02 = 2;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_03 = 4;

    @Override
    public void serialize(final PortMod message, final ByteBuf outBuffer) {
        final int index = outBuffer.writerIndex();
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeInt(message.getPortNo().getValue().intValue());
        outBuffer.writeZero(PADDING_IN_PORT_MOD_MESSAGE_01);
        outBuffer.writeBytes(IetfYangUtil.INSTANCE.macAddressBytes(message.getHwAddress()));
        outBuffer.writeZero(PADDING_IN_PORT_MOD_MESSAGE_02);
        outBuffer.writeInt(createPortConfigBitmask(message.getConfig()));
        outBuffer.writeInt(createPortConfigBitmask(message.getMask()));
        outBuffer.writeInt(createPortFeaturesBitmask(message.getAdvertise()));
        outBuffer.writeZero(PADDING_IN_PORT_MOD_MESSAGE_03);
        ByteBufUtils.updateOFHeaderLength(outBuffer, index);
    }

    private static int createPortConfigBitmask(final PortConfig config) {
        Map<Integer, Boolean> portConfigMap = new HashMap<>();
        portConfigMap.put(0, config.getPortDown());
        portConfigMap.put(2, config.getNoRecv());
        portConfigMap.put(5, config.getNoFwd());
        portConfigMap.put(6, config.getNoPacketIn());

        return ByteBufUtils.fillBitMaskFromMap(portConfigMap);
    }

    private static int createPortFeaturesBitmask(final PortFeatures feature) {
        return ByteBufUtils.fillBitMask(0, feature.get_10mbHd(),
                feature.get_10mbFd(),
                feature.get_100mbHd(),
                feature.get_100mbFd(),
                feature.get_1gbHd(),
                feature.get_1gbFd(),
                feature.get_10gbFd(),
                feature.get_40gbFd(),
                feature.get_100gbFd(),
                feature.get_1tbFd(),
                feature.getOther(),
                feature.getCopper(),
                feature.getFiber(),
                feature.getAutoneg(),
                feature.getPause(),
                feature.getPauseAsym());
    }

}
