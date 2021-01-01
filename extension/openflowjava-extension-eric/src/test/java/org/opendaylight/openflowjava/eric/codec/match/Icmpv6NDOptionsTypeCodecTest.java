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
import org.junit.Test;
import org.opendaylight.openflowjava.eric.api.EricConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EricExpClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.Icmpv6NdOptionsType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.icmpv6.nd.options.type.grouping.Icmpv6NdOptionsTypeValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.oxm.container.match.entry.value.Icmpv6NdOptionsTypeCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.oxm.container.match.entry.value.Icmpv6NdOptionsTypeCaseValueBuilder;
import org.opendaylight.yangtools.yang.common.Uint8;

public class Icmpv6NDOptionsTypeCodecTest {

    private static final int ERIC_FIELD_CODE = 2;
    private static final int VALUE_LENGTH = 1;

    private final Icmpv6NDOptionsTypeCodec icmpv6NDOptionsTypeCodec = new Icmpv6NDOptionsTypeCodec();
    private final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();

    private MatchEntry input;

    @Test
    public void serializeTest() {
        input = createMatchEntry();
        icmpv6NDOptionsTypeCodec.serialize(input, buffer);

        assertEquals(EricConstants.ERICOXM_OF_EXPERIMENTER_ID,
                buffer.readUnsignedShort());
        short fieldMask = buffer.readUnsignedByte();
        assertEquals(ERIC_FIELD_CODE, fieldMask >> 1);
        assertEquals(0, fieldMask & 1);
        assertEquals(VALUE_LENGTH, buffer.readUnsignedByte());
        assertEquals(1, buffer.readUnsignedByte());
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);

        input = icmpv6NDOptionsTypeCodec.deserialize(buffer);

        final Icmpv6NdOptionsTypeCaseValue result = (Icmpv6NdOptionsTypeCaseValue) input.getMatchEntryValue();
        assertEquals(EricExpClass.class, input.getOxmClass());
        assertEquals(Icmpv6NdOptionsType.class, input.getOxmMatchField());
        assertEquals(false, input.getHasMask());
        assertEquals(2, result.getIcmpv6NdOptionsTypeValues().getIcmpv6NdOptionsType().shortValue());
    }

    private static MatchEntry createMatchEntry() {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        final Icmpv6NdOptionsTypeCaseValueBuilder caseBuilder = new Icmpv6NdOptionsTypeCaseValueBuilder();
        final Icmpv6NdOptionsTypeValuesBuilder valuesBuilder = new Icmpv6NdOptionsTypeValuesBuilder();

        matchEntryBuilder.setOxmClass(EricExpClass.class);
        matchEntryBuilder.setOxmMatchField(Icmpv6NdOptionsType.class);
        matchEntryBuilder.setHasMask(false);

        valuesBuilder.setIcmpv6NdOptionsType(Uint8.ONE);

        caseBuilder.setIcmpv6NdOptionsTypeValues(valuesBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static void createBuffer(final ByteBuf message) {
        message.writeShort(EricConstants.ERICOXM_OF_EXPERIMENTER_ID);

        int fieldMask = ERIC_FIELD_CODE << 1;
        message.writeByte(fieldMask);
        message.writeByte(VALUE_LENGTH);
        message.writeByte(2);
    }
}