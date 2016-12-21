/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import java.util.Objects;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TcpFlagsMatchBuilder;

import io.netty.buffer.ByteBuf;

public class TcpFlagsEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        final boolean hasMask = processHeader(message);
        message.readUnsignedInt(); // Just skip experimenter ID for now, not used

        final TcpFlagsMatchBuilder tcpFlagsBuilder = new TcpFlagsMatchBuilder()
            .setTcpFlags(message.readUnsignedShort());

        if (hasMask) {
            tcpFlagsBuilder.setTcpFlagsMask(message.readUnsignedShort());
        }

        if (Objects.isNull(builder.getTcpFlagsMatch())) {
            builder.setTcpFlagsMatch(tcpFlagsBuilder.build());
        } else {
            throwErrorOnMalformed(builder, "tcpFlagsMatch");
        }
    }

}
