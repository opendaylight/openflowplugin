/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.PacketTypeMatchBuilder;

public class PacketTypeEntryDeserializer extends AbstractMatchEntryDeserializer {
    @Override
    public void deserializeEntry(final ByteBuf message, final MatchBuilder builder) {
        if (builder.getPacketTypeMatch() != null) {
            throwErrorOnMalformed(builder, "packet-type");
        }

        processHeader(message);
        builder.setPacketTypeMatch(new PacketTypeMatchBuilder().setPacketType(readUint32(message)).build());
    }
}
