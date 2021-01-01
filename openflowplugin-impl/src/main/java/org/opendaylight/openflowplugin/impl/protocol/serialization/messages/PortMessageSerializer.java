/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import io.netty.buffer.ByteBuf;
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
public class PortMessageSerializer extends AbstractMessageSerializer<PortMessage> {

    private static final byte PADDING_IN_PORT_MOD_MESSAGE_01 = 4;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_02 = 2;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_03 = 4;
    private static final Integer DEFAULT_PORT_CONFIG_MASK = createPortConfigBitMask(
            new PortConfig(true, true, true, true));

    @Override
    public void serialize(final PortMessage message, final ByteBuf outBuffer) {
        final int index = outBuffer.writerIndex();
        super.serialize(message, outBuffer);
        outBuffer.writeInt(OpenflowPortsUtil
                .getProtocolPortNumber(OpenflowVersion.OF13, message.getPortNumber()).intValue());
        outBuffer.writeZero(PADDING_IN_PORT_MOD_MESSAGE_01);
        outBuffer.writeBytes(IetfYangUtil.INSTANCE.macAddressBytes(message.getHardwareAddress()));
        outBuffer.writeZero(PADDING_IN_PORT_MOD_MESSAGE_02);
        outBuffer.writeInt(createPortConfigBitMask(message.getConfiguration()));
        outBuffer.writeInt(MoreObjects
                .firstNonNull(createPortConfigBitMask(message.getMask()), DEFAULT_PORT_CONFIG_MASK));
        outBuffer.writeInt(createPortFeaturesBitMask(message.getAdvertisedFeatures()));
        outBuffer.writeZero(PADDING_IN_PORT_MOD_MESSAGE_03);
        outBuffer.setShort(index + 2, outBuffer.writerIndex() - index);
    }

    @Override
    protected byte getMessageType() {
        return 16;
    }

    private static Integer createPortConfigBitMask(final PortConfig config) {
        return config == null ? null : ByteBufUtils.fillBitMaskFromMap(ImmutableMap
                .<Integer, Boolean>builder()
                .put(0, config.getPORTDOWN())
                .put(2, config.getNORECV())
                .put(5, config.getNOFWD())
                .put(6, config.getNOPACKETIN())
                .build());
    }

    private static int createPortFeaturesBitMask(final PortFeatures feature) {
        return ByteBufUtils.fillBitMask(0,
                feature.getTenMbHd(),
                feature.getTenMbFd(),
                feature.getHundredMbHd(),
                feature.getHundredMbFd(),
                feature.getOneGbHd(),
                feature.getOneGbFd(),
                feature.getTenGbFd(),
                feature.getFortyGbFd(),
                feature.getHundredGbFd(),
                feature.getOneTbFd(),
                feature.getOther(),
                feature.getCopper(),
                feature.getFiber(),
                feature.getAutoeng(),
                feature.getPause(),
                feature.getPauseAsym());
    }
}
