/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.metadata._case.MetadataBuilder;

/**
 * Translates OxmMetadata messages.
 *
 * @author michal.polkorab
 */
public class OxmMetadataDeserializer extends AbstractOxmMatchEntryDeserializer {
    public OxmMetadataDeserializer() {
        super(Metadata.class);
    }

    @Override
    protected void deserialize(final ByteBuf input, final MatchEntryBuilder builder) {
        final byte[] metadataBytes = new byte[Long.BYTES];
        input.readBytes(metadataBytes);

        final MetadataBuilder metadataBuilder = new MetadataBuilder().setMetadata(metadataBytes);
        if (builder.getHasMask()) {
            metadataBuilder.setMask(OxmDeserializerHelper.convertMask(input, Long.BYTES));
        }
        builder.setMatchEntryValue(new MetadataCaseBuilder().setMetadata(metadataBuilder.build()).build());
    }
}
