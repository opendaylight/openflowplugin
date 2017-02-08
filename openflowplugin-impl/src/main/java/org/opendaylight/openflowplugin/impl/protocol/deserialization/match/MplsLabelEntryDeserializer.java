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

public class MplsLabelEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        processHeader(message);
        final long mplsLabel = message.readUnsignedInt();

        if (Objects.isNull(builder.getProtocolMatchFields())) {
            builder.setProtocolMatchFields(new ProtocolMatchFieldsBuilder()
                    .setMplsLabel(mplsLabel)
                    .build());
        } else if (Objects.isNull(builder.getProtocolMatchFields().getMplsLabel())) {
            builder.setProtocolMatchFields(new ProtocolMatchFieldsBuilder(builder.getProtocolMatchFields())
                    .setMplsLabel(mplsLabel)
                    .build());
        } else {
            throwErrorOnMalformed(builder, "protocolMatchFields", "mplsLabel");
        }
    }

}
