/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.packet.type.PacketType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.packet.type.PacketTypeBuilder;

public class PacketTypeEntryDeserializer extends AbstractMatchEntryDeserializer {
    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {

        if (Objects.nonNull(builder.getPacketType())) {
            throwErrorOnMalformed(builder, "packet-type");
        }

        processHeader(message);
        final int namespace = message.readUnsignedShort();
        final int ns_type = message.readUnsignedShort();
        final PacketType packetType = new PacketTypeBuilder().setNamespace(namespace).setType(ns_type).build();
        builder.setPacketType(packetType);
    }
}
