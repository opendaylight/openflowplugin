/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessageBuilder;

/**
 * Translates PacketIn messages (OpenFlow v1.0).
 *
 * @author michal.polkorab
 */
public class OF10PacketInMessageFactory implements OFDeserializer<PacketInMessage> {

    private static final byte PADDING_IN_PACKET_IN_HEADER = 1;

    @Override
    public PacketInMessage deserialize(final ByteBuf rawMessage) {
        PacketInMessageBuilder builder = new PacketInMessageBuilder();
        builder.setVersion((short) EncodeConstants.OF10_VERSION_ID);
        builder.setXid(rawMessage.readUnsignedInt());
        builder.setBufferId(rawMessage.readUnsignedInt());
        builder.setTotalLen(rawMessage.readUnsignedShort());
        builder.setInPort(rawMessage.readUnsignedShort());
        builder.setReason(PacketInReason.forValue(rawMessage.readUnsignedByte()));
        rawMessage.skipBytes(PADDING_IN_PACKET_IN_HEADER);
        int remainingBytes = rawMessage.readableBytes();
        if (remainingBytes > 0) {
            final byte[] buf = new byte[remainingBytes];
            rawMessage.readBytes(buf);
            builder.setData(buf);
        }
        return builder.build();
    }
}
