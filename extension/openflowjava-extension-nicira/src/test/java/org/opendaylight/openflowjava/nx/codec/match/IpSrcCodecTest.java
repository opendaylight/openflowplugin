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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfIpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.IpSrcCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.IpSrcCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.ip.src.grouping.IpSrcValuesBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

public class IpSrcCodecTest {
    private final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
    private final IpSrcCodec ipSrcCodec = new IpSrcCodec();

    private MatchEntry input;

    private static final int VALUE_LENGTH = 4;
    private static final int NXM_FIELD_CODE = 7;

    @Test
    public void serializeTest() {
        input = createMatchEntry();
        ipSrcCodec.serialize(input, buffer);

        assertEquals(OxmMatchConstants.NXM_0_CLASS, buffer.readUnsignedShort());
        short fieldMask = buffer.readUnsignedByte();
        assertEquals(NXM_FIELD_CODE, fieldMask >> 1);
        assertEquals(0, fieldMask & 1);
        assertEquals(VALUE_LENGTH, buffer.readUnsignedByte());
        assertEquals(1, buffer.readUnsignedInt());
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);

        input = ipSrcCodec.deserialize(buffer);

        final IpSrcCaseValue result = (IpSrcCaseValue) input.getMatchEntryValue();

        assertEquals(Nxm0Class.VALUE, input.getOxmClass());
        assertEquals(NxmOfIpSrc.VALUE, input.getOxmMatchField());
        assertEquals(false, input.getHasMask());
        assertEquals(2, result.getIpSrcValues().getValue().intValue());
    }

    private static MatchEntry createMatchEntry() {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        final IpSrcCaseValueBuilder caseBuilder = new IpSrcCaseValueBuilder();
        final IpSrcValuesBuilder valuesBuilder = new IpSrcValuesBuilder();

        matchEntryBuilder.setOxmClass(Nxm0Class.VALUE);
        matchEntryBuilder.setOxmMatchField(NxmOfIpSrc.VALUE);
        matchEntryBuilder.setHasMask(false);

        valuesBuilder.setValue(Uint32.ONE);

        caseBuilder.setIpSrcValues(valuesBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static void createBuffer(final ByteBuf message) {
        message.writeShort(OxmMatchConstants.NXM_0_CLASS);

        int fieldMask = NXM_FIELD_CODE << 1;
        message.writeByte(fieldMask);
        message.writeByte(VALUE_LENGTH);
        message.writeInt(2);
    }
}
