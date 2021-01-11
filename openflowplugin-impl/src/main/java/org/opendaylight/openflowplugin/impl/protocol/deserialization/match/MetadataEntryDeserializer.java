/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint64;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.MetadataBuilder;

public class MetadataEntryDeserializer extends AbstractMatchEntryDeserializer {
    @Override
    public void deserializeEntry(final ByteBuf message, final MatchBuilder builder) {
        final boolean hasMask = processHeader(message);
        final MetadataBuilder metadataBuilder = new MetadataBuilder()
                .setMetadata(readUint64(message));

        if (hasMask) {
            metadataBuilder.setMetadataMask(readUint64(message));
        }

        final Metadata metadata = builder.getMetadata();
        if (metadata == null) {
            builder.setMetadata(metadataBuilder.build());
        } else {
            throwErrorOnMalformed(builder, "metadata");
        }
    }
}
