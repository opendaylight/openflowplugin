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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxTunIpv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.TunIpv4SrcCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.TunIpv4SrcCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.tun.ipv4.src.grouping.TunIpv4SrcValuesBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

public class TunIpv4SrcCodecTest {
    private final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
    private final TunIpv4SrcCodec tunIpv4SrcCodec = new TunIpv4SrcCodec();

    private MatchEntry input;

    private static final int VALUE_LENGTH = 4;
    private static final int NXM_FIELD_CODE = 31;

    @Test
    public void serializeTest() {
        input = createMatchEntry();
        tunIpv4SrcCodec.serialize(input, buffer);

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

        input = tunIpv4SrcCodec.deserialize(buffer);

        final TunIpv4SrcCaseValue result = (TunIpv4SrcCaseValue) input.getMatchEntryValue();

        assertEquals(Nxm1Class.VALUE, input.getOxmClass());
        assertEquals(NxmNxTunIpv4Src.VALUE, input.getOxmMatchField());
        assertEquals(false, input.getHasMask());
        assertEquals(1, result.getTunIpv4SrcValues().getValue().intValue());
    }

    private static MatchEntry createMatchEntry() {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        final TunIpv4SrcCaseValueBuilder caseBuilder = new TunIpv4SrcCaseValueBuilder();
        final TunIpv4SrcValuesBuilder valuesBuilder = new TunIpv4SrcValuesBuilder();

        matchEntryBuilder.setOxmClass(Nxm1Class.VALUE);
        matchEntryBuilder.setOxmMatchField(NxmNxTunIpv4Src.VALUE);
        matchEntryBuilder.setHasMask(false);

        valuesBuilder.setValue(Uint32.ONE);

        caseBuilder.setTunIpv4SrcValues(valuesBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static void createBuffer(final ByteBuf message) {
        message.writeShort(OxmMatchConstants.NXM_1_CLASS);

        int fieldMask = NXM_FIELD_CODE << 1;
        message.writeByte(fieldMask);
        message.writeByte(VALUE_LENGTH);
        message.writeInt(1);
    }
}
