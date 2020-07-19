/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
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
import org.opendaylight.openflowjava.protocol.impl.util.OpenflowUtils;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessageBuilder;

/**
 * Translates PortStatus messages (OpenFlow v1.0).
 *
 * @author michal.polkorab
 */
public class OF10PortStatusMessageFactory implements OFDeserializer<PortStatusMessage> {

    private static final byte PADDING_IN_PORT_STATUS_HEADER = 7;

    @Override
    public PortStatusMessage deserialize(final ByteBuf rawMessage) {
        PortStatusMessageBuilder builder = new PortStatusMessageBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_0)
                .setXid(readUint32(rawMessage))
                .setReason(PortReason.forValue(rawMessage.readUnsignedByte()));
        rawMessage.skipBytes(PADDING_IN_PORT_STATUS_HEADER);
        deserializePort(rawMessage, builder);
        return builder.build();
    }

    private static void deserializePort(final ByteBuf rawMessage, final PortStatusMessageBuilder builder) {
        builder.setPortNo(readUint16(rawMessage).toUint32());
        builder.setHwAddr(ByteBufUtils.readIetfMacAddress(rawMessage));
        builder.setName(ByteBufUtils.decodeNullTerminatedString(rawMessage, EncodeConstants.MAX_PORT_NAME_LENGTH));
        builder.setConfigV10(OpenflowUtils.createPortConfig(rawMessage.readUnsignedInt()));
        builder.setStateV10(OpenflowUtils.createPortState(rawMessage.readUnsignedInt()));
        builder.setCurrentFeaturesV10(OpenflowUtils.createPortFeatures(rawMessage.readUnsignedInt()));
        builder.setAdvertisedFeaturesV10(OpenflowUtils.createPortFeatures(rawMessage.readUnsignedInt()));
        builder.setSupportedFeaturesV10(OpenflowUtils.createPortFeatures(rawMessage.readUnsignedInt()));
        builder.setPeerFeaturesV10(OpenflowUtils.createPortFeatures(rawMessage.readUnsignedInt()));
    }
}
