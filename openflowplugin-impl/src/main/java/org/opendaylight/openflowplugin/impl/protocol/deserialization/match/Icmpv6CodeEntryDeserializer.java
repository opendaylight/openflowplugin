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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv6MatchBuilder;

public class Icmpv6CodeEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        processHeader(message);
        final short code = message.readUnsignedByte();

        if (builder.getIcmpv6Match() == null) {
            builder.setIcmpv6Match(new Icmpv6MatchBuilder()
                    .setIcmpv6Code(code)
                    .build());
        } else if (builder.getIcmpv6Match().getIcmpv6Code() == null) {
            builder.setIcmpv6Match(new Icmpv6MatchBuilder(builder.getIcmpv6Match())
                    .setIcmpv6Code(code)
                    .build());
        } else {
            throwErrorOnMalformed(builder, "icmpv6Match", "icmpv6Code");
        }
    }
}
