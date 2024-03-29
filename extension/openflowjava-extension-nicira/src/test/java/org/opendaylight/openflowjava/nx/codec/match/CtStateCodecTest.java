/*
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
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxCtState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.CtStateCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.CtStateCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.ct.state.grouping.CtStateValuesBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

public class CtStateCodecTest {
    private final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
    private final CtStateCodec ctStateCodec = new CtStateCodec();

    private MatchEntry input;

    private static final int VALUE_LENGTH = 8;
    private static final int NXM_FIELD_CODE = 105;

    @Test
    public void serializeTest() {
        input = createMatchEntry();
        ctStateCodec.serialize(input, buffer);

        assertEquals(OxmMatchConstants.NXM_1_CLASS, buffer.readUnsignedShort());
        short fieldMask = buffer.readUnsignedByte();
        assertEquals(NXM_FIELD_CODE, fieldMask >> 1);
        assertEquals(1, fieldMask & 1);
        assertEquals(VALUE_LENGTH, buffer.readUnsignedByte());
        assertEquals(1, buffer.readUnsignedInt());
        assertEquals(2, buffer.readUnsignedInt());
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);
        input = ctStateCodec.deserialize(buffer);

        final CtStateCaseValue result = (CtStateCaseValue) input.getMatchEntryValue();

        assertEquals(Nxm1Class.VALUE, input.getOxmClass());
        assertEquals(NxmNxCtState.VALUE, input.getOxmMatchField());
        assertEquals(true, input.getHasMask());
        assertEquals(1, result.getCtStateValues().getCtState().intValue());
        assertEquals(2, result.getCtStateValues().getMask().intValue());
    }

    private static MatchEntry createMatchEntry() {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        final CtStateCaseValueBuilder caseBuilder = new CtStateCaseValueBuilder();
        final CtStateValuesBuilder valuesBuilder = new CtStateValuesBuilder();

        matchEntryBuilder.setOxmClass(Nxm1Class.VALUE);
        matchEntryBuilder.setOxmMatchField(NxmNxCtState.VALUE);
        matchEntryBuilder.setHasMask(true);

        valuesBuilder.setCtState(Uint32.ONE);
        valuesBuilder.setMask(Uint32.TWO);

        caseBuilder.setCtStateValues(valuesBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static void createBuffer(final ByteBuf message) {
        message.writeShort(OxmMatchConstants.NXM_1_CLASS);

        int fieldMask = NXM_FIELD_CODE << 1;
        message.writeByte(fieldMask);
        message.writeByte(VALUE_LENGTH);
        //CtState = 1
        message.writeInt(1);
        //Mask = 2
        message.writeInt(2);
    }
}
