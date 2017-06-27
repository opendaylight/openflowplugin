/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.metadata._case.MetadataBuilder;

/**
 * @author michal.polkorab
 *
 */
public class OxmMetadataDeserializer  extends AbstractOxmMatchEntryDeserializer
        implements OFDeserializer<MatchEntry> {

    @Override
    public MatchEntry deserialize(ByteBuf input) {
        MatchEntryBuilder builder = processHeader(getOxmClass(), getOxmField(), input);
        addMetadataValue(input, builder);
        return builder.build();
    }

    private static void addMetadataValue(ByteBuf input, MatchEntryBuilder builder) {
        MetadataCaseBuilder caseBuilder = new MetadataCaseBuilder();
        MetadataBuilder metadataBuilder = new MetadataBuilder();
        byte[] metadataBytes = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        input.readBytes(metadataBytes);
        metadataBuilder.setMetadata(metadataBytes);
        if (builder.isHasMask()) {
            metadataBuilder.setMask(OxmDeserializerHelper
                    .convertMask(input, EncodeConstants.SIZE_OF_LONG_IN_BYTES));
        }
        caseBuilder.setMetadata(metadataBuilder.build());
        builder.setMatchEntryValue(caseBuilder.build());
    }

    @Override
    protected Class<? extends MatchField> getOxmField() {
        return Metadata.class;
    }

    @Override
    protected Class<? extends OxmClassBase> getOxmClass() {
        return OpenflowBasicClass.class;
    }
}
