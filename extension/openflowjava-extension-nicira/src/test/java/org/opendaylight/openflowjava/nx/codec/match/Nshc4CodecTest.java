/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.match;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshc4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nshc._4.grouping.Nshc4ValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.Nshc4CaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.Nshc4CaseValueBuilder;

public class Nshc4CodecTest {

    Nshc4Codec nshc4Codec;
    ByteBuf buffer;
    MatchEntry input;

    private static final int VALUE_LENGTH = 4;
    private static final int NXM_FIELD_CODE = 118;

    @Before
    public void setUp() {
        nshc4Codec = new Nshc4Codec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTest() {
        input = createMatchEntry();
        nshc4Codec.serialize(input, buffer);

        assertEquals(OxmMatchConstants.NXM_1_CLASS, buffer.readUnsignedShort());
        short fieldMask = buffer.readUnsignedByte();
        assertEquals(NXM_FIELD_CODE, fieldMask >> 1);
        assertEquals(0, fieldMask & 1);
        assertEquals(VALUE_LENGTH, buffer.readUnsignedByte());
        assertEquals(1, buffer.readUnsignedInt());
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);

        input = nshc4Codec.deserialize(buffer);

        Nshc4CaseValue result = ((Nshc4CaseValue) input.getMatchEntryValue());

        assertEquals(Nxm1Class.class, input.getOxmClass());
        assertEquals(NxmNxNshc4.class, input.getOxmMatchField());
        assertEquals(false, input.isHasMask());
        assertEquals(2, result.getNshc4Values().getNshc().intValue());
    }


    private MatchEntry createMatchEntry() {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        Nshc4CaseValueBuilder caseBuilder = new Nshc4CaseValueBuilder();
        Nshc4ValuesBuilder valuesBuilder = new Nshc4ValuesBuilder();

        matchEntryBuilder.setOxmClass(Nxm1Class.class);
        matchEntryBuilder.setOxmMatchField(NxmNxNshc4.class);
        matchEntryBuilder.setHasMask(false);

        valuesBuilder.setNshc((long)1);

        caseBuilder.setNshc4Values(valuesBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        return matchEntryBuilder.build();
    }

    private void createBuffer(ByteBuf message) {
        message.writeShort(OxmMatchConstants.NXM_1_CLASS);

        int fieldMask = (NXM_FIELD_CODE << 1);
        message.writeByte(fieldMask);
        message.writeByte(VALUE_LENGTH);
        message.writeInt(2);
    }
}
