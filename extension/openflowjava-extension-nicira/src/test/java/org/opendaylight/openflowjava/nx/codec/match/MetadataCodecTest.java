/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.match;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfMetadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.OfMetadataCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.oxm.of.metadata.grouping.MetadataValuesBuilder;

public class MetadataCodecTest {
    private final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
    private final MetadataCodec metadataCodec = new MetadataCodec();

    private MatchEntry input;

    private static final int NXM_FIELD_CODE = 2;
    private static final int VALUE_LENGTH = 8;

    @Test
    public void serializeTest() {
        input = createMatchEntry();
        metadataCodec.serialize(input, buffer);

        assertEquals(OxmMatchConstants.OPENFLOW_BASIC_CLASS, buffer.readUnsignedShort());
        short fieldMask = buffer.readUnsignedByte();
        assertEquals(NXM_FIELD_CODE, fieldMask >> 1);
        assertEquals(0, fieldMask & 1);
        assertEquals(VALUE_LENGTH, buffer.readUnsignedByte());
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);
        input = metadataCodec.deserialize(buffer);
        assertEquals(Nxm0Class.VALUE, input.getOxmClass());
        assertEquals(NxmOfMetadata.VALUE, input.getOxmMatchField());
        assertEquals(false, input.getHasMask());
    }

    private static MatchEntry createMatchEntry() {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(Nxm0Class.VALUE);
        matchEntryBuilder.setOxmMatchField(NxmOfMetadata.VALUE);
        matchEntryBuilder.setHasMask(false);

        OfMetadataCaseValueBuilder caseBuilder = new OfMetadataCaseValueBuilder();
        MetadataValuesBuilder valuesBuilder = new MetadataValuesBuilder();

        caseBuilder.setMetadataValues(valuesBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static void createBuffer(final ByteBuf message) {
        message.writeShort(OxmMatchConstants.NXM_1_CLASS);
        int fieldMask = NXM_FIELD_CODE << 1;
        message.writeByte(fieldMask);
        message.writeByte(VALUE_LENGTH);
    }
}