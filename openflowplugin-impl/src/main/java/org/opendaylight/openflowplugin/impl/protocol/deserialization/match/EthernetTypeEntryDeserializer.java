/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;

public class EthernetTypeEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        processHeader(message);
        final int type = message.readUnsignedShort();

        if (Objects.isNull(builder.getEthernetMatch())) {
            builder.setEthernetMatch(new EthernetMatchBuilder()
                    .setEthernetType(new EthernetTypeBuilder()
                            .setType(new EtherType(Long.valueOf(type)))
                            .build())
                    .build());
        } else if (Objects.isNull(builder.getEthernetMatch().getEthernetType())) {
            builder.setEthernetMatch(new EthernetMatchBuilder(builder.getEthernetMatch())
                    .setEthernetType(new EthernetTypeBuilder()
                            .setType(new EtherType(Long.valueOf(type)))
                            .build())
                    .build());
        } else {
            throwErrorOnMalformed(builder, "ethernetMatch", "ethernetType");
        }
    }

}
