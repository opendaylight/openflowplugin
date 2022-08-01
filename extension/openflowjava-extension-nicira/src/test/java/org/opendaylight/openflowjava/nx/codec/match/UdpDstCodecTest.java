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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfUdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.UdpDstCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.UdpDstCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.udp.dst.grouping.UdpDstValuesBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

public class UdpDstCodecTest {
    private final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
    private final UdpDstCodec udpDstCodec = new UdpDstCodec();

    private MatchEntry input;

    private static final int VALUE_LENGTH = 4;
    private static final int NXM_FIELD_CODE = 12;

    @Test
    public void serializeTest() {
        input = createMatchEntry();
        udpDstCodec.serialize(input, buffer);

        assertEquals(OxmMatchConstants.NXM_0_CLASS, buffer.readUnsignedShort());
        short fieldMask = buffer.readUnsignedByte();
        assertEquals(NXM_FIELD_CODE, fieldMask >> 1);
        assertEquals(1, fieldMask & 1);
        assertEquals(VALUE_LENGTH, buffer.readUnsignedByte());
        assertEquals(1, buffer.readUnsignedShort());
        assertEquals(0xffff, buffer.readUnsignedShort());
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);

        input = udpDstCodec.deserialize(buffer);

        final UdpDstCaseValue result = (UdpDstCaseValue) input.getMatchEntryValue();

        assertEquals(Nxm0Class.VALUE, input.getOxmClass());
        assertEquals(NxmOfUdpDst.VALUE, input.getOxmMatchField());
        assertEquals(true, input.getHasMask());
        assertEquals(1, result.getUdpDstValues().getPort().getValue().shortValue());
        assertEquals(0xffff, result.getUdpDstValues().getMask().shortValue() & 0xffff);
    }

    private static MatchEntry createMatchEntry() {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        final UdpDstCaseValueBuilder caseBuilder = new UdpDstCaseValueBuilder();
        final UdpDstValuesBuilder valuesBuilder = new UdpDstValuesBuilder();

        matchEntryBuilder.setOxmClass(Nxm0Class.VALUE);
        matchEntryBuilder.setOxmMatchField(NxmOfUdpDst.VALUE);
        matchEntryBuilder.setHasMask(true);

        valuesBuilder.setPort(new PortNumber(Uint16.ONE));
        valuesBuilder.setMask(Uint16.MAX_VALUE);

        caseBuilder.setUdpDstValues(valuesBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static void createBuffer(final ByteBuf message) {
        message.writeShort(OxmMatchConstants.NXM_0_CLASS);

        int fieldMask = NXM_FIELD_CODE << 1;
        message.writeByte(fieldMask);
        message.writeByte(VALUE_LENGTH);
        //Port num = 1
        message.writeShort(1);
        message.writeShort(0xffff);
    }
}
