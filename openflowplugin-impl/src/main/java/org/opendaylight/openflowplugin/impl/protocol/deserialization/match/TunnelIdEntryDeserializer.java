/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TunnelBuilder;

public class TunnelIdEntryDeserializer extends AbstractMatchEntryDeserializer {
    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        final boolean hasMask = processHeader(message);

        final byte[] tunnelId = new byte[Long.BYTES];
        message.readBytes(tunnelId);
        final TunnelBuilder tunnelBuilder = new TunnelBuilder()
            .setTunnelId(new BigInteger(1, tunnelId));

        if (hasMask) {
            final byte[] tunnelMask = new byte[Long.BYTES];
            message.readBytes(tunnelMask);
            tunnelBuilder.setTunnelMask(new BigInteger(1, tunnelMask));
        }

        if (builder.getTunnel() == null) {
            builder.setTunnel(tunnelBuilder.build());
        } else {
            throwErrorOnMalformed(builder, "tunnel");
        }
    }
}
