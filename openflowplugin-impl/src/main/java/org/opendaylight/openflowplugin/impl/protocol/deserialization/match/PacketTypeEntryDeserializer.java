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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.PacketTypeMatchBuilder;

public class PacketTypeEntryDeserializer extends AbstractMatchEntryDeserializer {
    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {

        if (Objects.nonNull(builder.getPacketTypeMatch())) {
            throwErrorOnMalformed(builder, "packet-type");
        }

        processHeader(message);
        final long packetType = message.readUnsignedInt();
        builder.setPacketTypeMatch(new PacketTypeMatchBuilder().setPacketType(packetType).build());
    }
}
