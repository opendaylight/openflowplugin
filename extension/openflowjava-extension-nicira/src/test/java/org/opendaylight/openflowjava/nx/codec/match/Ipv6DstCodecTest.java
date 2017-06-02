/**
 * Copyright (c) 2017 Red Hat, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfIpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.ip.dst.grouping.IpDstValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.IpDstCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.IpDstCaseValueBuilder;

public class Ipv6DstCodecTest {

    Ipv6DstCodec ipv6DstCodec;
    ByteBuf buffer;
    MatchEntry input;

    private static final int VALUE_LENGTH = 16;
    private static final int NXM_FIELD_CODE = 20;

    @Before
    public void setUp() {
        ipv6DstCodec = new Ipv6DstCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTest() {
        input = createMatchEntry();
        ipv6DstCodec.serialize(input, buffer);

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

        input = ipv6DstCodec.deserialize(buffer);

        IpDstCaseValue result = ((IpDstCaseValue) input.getMatchEntryValue());

        assertEquals(Nxm1Class.class, input.getOxmClass());
        assertEquals(NxmOfIpDst.class, input.getOxmMatchField());
        assertEquals(false, input.isHasMask());
        assertEquals(2, result.getIpDstValues().getValue().intValue());
    }


    private MatchEntry createMatchEntry() {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        IpDstCaseValueBuilder caseBuilder = new IpDstCaseValueBuilder();
        IpDstValuesBuilder valuesBuilder = new IpDstValuesBuilder();

        matchEntryBuilder.setOxmClass(Nxm1Class.class);
        matchEntryBuilder.setOxmMatchField(NxmOfIpDst.class);
        matchEntryBuilder.setHasMask(false);

        valuesBuilder.setValue((long)1);

        caseBuilder.setIpDstValues(valuesBuilder.build());
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
