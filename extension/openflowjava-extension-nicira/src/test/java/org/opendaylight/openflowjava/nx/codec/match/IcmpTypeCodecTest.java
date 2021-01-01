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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfIcmpType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.icmp.type.grouping.IcmpTypeValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.IcmpTypeCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.IcmpTypeCaseValueBuilder;
import org.opendaylight.yangtools.yang.common.Uint8;

public class IcmpTypeCodecTest {
    private final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
    private final IcmpTypeCodec icmpTypeCodec = new IcmpTypeCodec();

    private MatchEntry input;

    private static final int VALUE_LENGTH = 1;
    private static final int NXM_FIELD_CODE = 13;

    @Test
    public void serializeTest() {
        input = createMatchEntry();
        icmpTypeCodec.serialize(input, buffer);

        assertEquals(OxmMatchConstants.NXM_0_CLASS, buffer.readUnsignedShort());
        short fieldMask = buffer.readUnsignedByte();
        assertEquals(NXM_FIELD_CODE, fieldMask >> 1);
        assertEquals(0, fieldMask & 1);
        assertEquals(VALUE_LENGTH, buffer.readUnsignedByte());
        assertEquals(1, buffer.readUnsignedByte());
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);

        input = icmpTypeCodec.deserialize(buffer);

        final IcmpTypeCaseValue result = (IcmpTypeCaseValue) input.getMatchEntryValue();

        assertEquals(Nxm0Class.class, input.getOxmClass());
        assertEquals(NxmOfIcmpType.class, input.getOxmMatchField());
        assertEquals(false, input.getHasMask());
        assertEquals(2, result.getIcmpTypeValues().getValue().shortValue());
    }

    private static MatchEntry createMatchEntry() {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        final IcmpTypeCaseValueBuilder caseBuilder = new IcmpTypeCaseValueBuilder();
        final IcmpTypeValuesBuilder valuesBuilder = new IcmpTypeValuesBuilder();

        matchEntryBuilder.setOxmClass(Nxm0Class.class);
        matchEntryBuilder.setOxmMatchField(NxmOfIcmpType.class);
        matchEntryBuilder.setHasMask(false);

        valuesBuilder.setValue(Uint8.ONE);

        caseBuilder.setIcmpTypeValues(valuesBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static void createBuffer(final ByteBuf message) {
        message.writeShort(OxmMatchConstants.NXM_0_CLASS);

        int fieldMask = NXM_FIELD_CODE << 1;
        message.writeByte(fieldMask);
        message.writeByte(VALUE_LENGTH);
        message.writeByte(2);
    }
}
