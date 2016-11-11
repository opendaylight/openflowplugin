/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortMessage;

/**
 * Translates PortMod messages.
 * OF protocol versions: 1.3.
 */
public class PortMessageSerializer implements OFSerializer<PortMessage> {
    private static final byte MESSAGE_TYPE = 16;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_01 = 4;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_02 = 2;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_03 = 4;

    @Override
    public void serialize(final PortMessage message, final ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeInt(OpenflowPortsUtil.getProtocolPortNumber(OpenflowVersion.OF13, message.getPortNumber()).intValue());
        outBuffer.writeZero(PADDING_IN_PORT_MOD_MESSAGE_01);
        outBuffer.writeBytes(IetfYangUtil.INSTANCE.bytesFor(message.getHardwareAddress()));
        outBuffer.writeZero(PADDING_IN_PORT_MOD_MESSAGE_02);
        final int portConfigBitmask = createPortConfigBitmask(message.getConfiguration());
        outBuffer.writeInt(portConfigBitmask); // Configuration
        outBuffer.writeInt(portConfigBitmask); // Configuration mask
        outBuffer.writeInt(createPortFeaturesBitmask(message.getAdvertisedFeatures()));
        outBuffer.writeZero(PADDING_IN_PORT_MOD_MESSAGE_03);
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    /**
     * @param config port config
     * @return port config bitmask
     */
    private static int createPortConfigBitmask(final PortConfig config) {
        return ByteBufUtils.fillBitMaskFromMap(ImmutableMap
                .<Integer, Boolean>builder()
                .put(0, config.isPORTDOWN())
                .put(2, config.isNORECV())
                .put(5, config.isNOFWD())
                .put(6, config.isNOPACKETIN())
                .build());
    }

    private static int createPortFeaturesBitmask(final PortFeatures feature) {
        return ByteBufUtils.fillBitMask(0,
                feature.isTenMbHd(),
                feature.isTenMbFd(),
                feature.isHundredMbHd(),
                feature.isHundredMbFd(),
                feature.isOneGbHd(),
                feature.isOneGbFd(),
                feature.isTenGbFd(),
                feature.isFortyGbFd(),
                feature.isHundredGbFd(),
                feature.isOneTbFd(),
                feature.isOther(),
                feature.isCopper(),
                feature.isFiber(),
                feature.isAutoeng(),
                feature.isPause(),
                feature.isPauseAsym());
    }

}
