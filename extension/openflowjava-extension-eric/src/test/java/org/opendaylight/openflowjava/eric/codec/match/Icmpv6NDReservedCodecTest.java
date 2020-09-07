/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.eric.codec.match;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.eric.api.EricConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EricExpClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.Icmpv6NdReserved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.icmpv6.nd.reserved.grouping.Icmpv6NdReservedValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.oxm.container.match.entry.value.Icmpv6NdReservedCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.oxm.container.match.entry.value.Icmpv6NdReservedCaseValueBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

public class Icmpv6NDReservedCodecTest {

    private static final int ERIC_FIELD_CODE = 1;
    private static final int VALUE_LENGTH = 4;

    private Icmpv6NDReservedCodec icmpv6NDReservedCodec;
    private ByteBuf buffer;
    private MatchEntry input;

    @Before
    public void setUp() {
        icmpv6NDReservedCodec = new Icmpv6NDReservedCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTest() {
        input = createMatchEntry();
        icmpv6NDReservedCodec.serialize(input, buffer);

        assertEquals(EricConstants.ERICOXM_OF_EXPERIMENTER_ID,
                buffer.readUnsignedShort());
        short fieldMask = buffer.readUnsignedByte();
        assertEquals(ERIC_FIELD_CODE, fieldMask >> 1);
        assertEquals(0, fieldMask & 1);
        assertEquals(VALUE_LENGTH, buffer.readUnsignedByte());
        assertEquals(1, buffer.readUnsignedInt());
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);
        input = icmpv6NDReservedCodec.deserialize(buffer);
        final Icmpv6NdReservedCaseValue result = (Icmpv6NdReservedCaseValue) input.getMatchEntryValue();

        assertEquals(EricExpClass.class, input.getOxmClass());
        assertEquals(Icmpv6NdReserved.class, input.getOxmMatchField());
        assertEquals(false, input.isHasMask());
        assertEquals(2, result.getIcmpv6NdReservedValues().getIcmpv6NdReserved().intValue());
    }

    private static MatchEntry createMatchEntry() {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        final Icmpv6NdReservedCaseValueBuilder caseBuilder = new Icmpv6NdReservedCaseValueBuilder();
        final Icmpv6NdReservedValuesBuilder valuesBuilder = new Icmpv6NdReservedValuesBuilder();

        matchEntryBuilder.setOxmClass(EricExpClass.class);
        matchEntryBuilder.setOxmMatchField(Icmpv6NdReserved.class);
        matchEntryBuilder.setHasMask(false);

        valuesBuilder.setIcmpv6NdReserved(Uint32.ONE);
        caseBuilder.setIcmpv6NdReservedValues(valuesBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static void createBuffer(ByteBuf message) {
        message.writeShort(EricConstants.ERICOXM_OF_EXPERIMENTER_ID);
        int fieldMask = ERIC_FIELD_CODE << 1;
        message.writeByte(fieldMask);
        message.writeByte(VALUE_LENGTH);
        message.writeInt(2);
    }
}