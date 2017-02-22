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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFieldsBuilder;

import io.netty.buffer.ByteBuf;

public class MplsBosEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        processHeader(message);
        final short mplsBos = message.readUnsignedByte();

        if (Objects.isNull(builder.getProtocolMatchFields())) {
            builder.setProtocolMatchFields(new ProtocolMatchFieldsBuilder()
                    .setMplsBos(mplsBos)
                    .build());
        } else if (Objects.isNull(builder.getProtocolMatchFields().getMplsBos())) {
            builder.setProtocolMatchFields(new ProtocolMatchFieldsBuilder(builder.getProtocolMatchFields())
                    .setMplsBos(mplsBos)
                    .build());
        } else {
            throwErrorOnMalformed(builder, "protocolMatchFields", "mplsBos");
        }
    }

}
