/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxCtTpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.CtTpDstCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.CtTpDstCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.ct.tp.dst.grouping.CtTpDstValuesBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

public class CtTpDstCodecTest {
    private final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
    private final CtTpDstCodec ctTpDstCodec = new CtTpDstCodec();

    private MatchEntry input;

    private static final int VALUE_LENGTH = 2;
    private static final int NXM_FIELD_CODE = 125;

    @Test
    public void serializeTest() {
        input = createMatchEntry();
        ctTpDstCodec.serialize(input, buffer);

        assertEquals(OxmMatchConstants.NXM_1_CLASS, buffer.readUnsignedShort());
        short fieldMask = buffer.readUnsignedByte();
        assertEquals(NXM_FIELD_CODE, fieldMask >> 1);
        assertEquals(0, fieldMask & 1);
        assertEquals(VALUE_LENGTH, buffer.readUnsignedByte());
        assertEquals(1, buffer.readUnsignedShort());
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);
        input = ctTpDstCodec.deserialize(buffer);

        final CtTpDstCaseValue result = (CtTpDstCaseValue) input.getMatchEntryValue();

        assertEquals(Nxm1Class.VALUE, input.getOxmClass());
        assertEquals(NxmNxCtTpDst.VALUE, input.getOxmMatchField());
        assertEquals(false, input.getHasMask());
        assertEquals(2, result.getCtTpDstValues().getCtTpDst().shortValue());
    }

    private static MatchEntry createMatchEntry() {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        final CtTpDstCaseValueBuilder caseBuilder = new CtTpDstCaseValueBuilder();
        final CtTpDstValuesBuilder valuesBuilder = new CtTpDstValuesBuilder();

        matchEntryBuilder.setOxmClass(Nxm1Class.VALUE);
        matchEntryBuilder.setOxmMatchField(NxmNxCtTpDst.VALUE);
        matchEntryBuilder.setHasMask(false);

        valuesBuilder.setCtTpDst(Uint16.ONE);

        caseBuilder.setCtTpDstValues(valuesBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static void createBuffer(final ByteBuf message) {
        message.writeShort(OxmMatchConstants.NXM_1_CLASS);

        int fieldMask = NXM_FIELD_CODE << 1;
        message.writeByte(fieldMask);
        message.writeByte(VALUE_LENGTH);
        message.writeShort(2);
    }
}
