/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4MatchBuilder;

public class Icmpv4TypeEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        processHeader(message);
        final short type = message.readUnsignedByte();

        if (builder.getIcmpv4Match() == null) {
            builder.setIcmpv4Match(new Icmpv4MatchBuilder()
                    .setIcmpv4Type(type)
                    .build());
        } else if (builder.getIcmpv4Match().getIcmpv4Type() == null) {
            builder.setIcmpv4Match(new Icmpv4MatchBuilder(builder.getIcmpv4Match())
                    .setIcmpv4Type(type)
                    .build());
        } else {
            throwErrorOnMalformed(builder, "icmpv4Match", "icmpv4Type");
        }
    }
}
