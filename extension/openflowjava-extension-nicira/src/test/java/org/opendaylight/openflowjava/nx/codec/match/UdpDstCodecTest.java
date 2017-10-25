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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfUdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.udp.dst.grouping.UdpDstValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.UdpDstCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.UdpDstCaseValueBuilder;

public class UdpDstCodecTest {

    UdpDstCodec udpDstCodec;
    ByteBuf buffer;
    MatchEntry input;

    private static final int VALUE_LENGTH = 4;
    private static final int NXM_FIELD_CODE = 12;

    @Before
    public void setUp() {
        udpDstCodec = new UdpDstCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

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

        UdpDstCaseValue result = ((UdpDstCaseValue) input.getMatchEntryValue());

        assertEquals(Nxm0Class.class, input.getOxmClass());
        assertEquals(NxmOfUdpDst.class, input.getOxmMatchField());
        assertEquals(true, input.isHasMask());
        assertEquals(1, result.getUdpDstValues().getPort().getValue().shortValue());
        assertEquals(0xffff, result.getUdpDstValues().getMask().shortValue() & 0xffff);
    }


    private MatchEntry createMatchEntry() {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        UdpDstCaseValueBuilder caseBuilder = new UdpDstCaseValueBuilder();
        UdpDstValuesBuilder valuesBuilder = new UdpDstValuesBuilder();

        matchEntryBuilder.setOxmClass(Nxm0Class.class);
        matchEntryBuilder.setOxmMatchField(NxmOfUdpDst.class);
        matchEntryBuilder.setHasMask(true);

        valuesBuilder.setPort(new PortNumber(1));
        valuesBuilder.setMask(0xffff);

        caseBuilder.setUdpDstValues(valuesBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        return matchEntryBuilder.build();
    }

    private void createBuffer(ByteBuf message) {
        message.writeShort(OxmMatchConstants.NXM_0_CLASS);

        int fieldMask = (NXM_FIELD_CODE << 1);
        message.writeByte(fieldMask);
        message.writeByte(VALUE_LENGTH);
        //Port num = 1
        message.writeShort(1);
        message.writeShort(0xffff);
    }
}
