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
import java.math.BigInteger;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxTunId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.tun.id.grouping.TunIdValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TunIdCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TunIdCaseValueBuilder;

public class TunIdCodecTest {

    TunIdCodec tunIdCodec;
    ByteBuf buffer;
    MatchEntry input;

    private static final int VALUE_LENGTH = 8;
    private static final int NXM_FIELD_CODE = 16;

    @Before
    public void setUp() {
        tunIdCodec = new TunIdCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTest() {
        input = createMatchEntry();
        tunIdCodec.serialize(input, buffer);

        assertEquals(OxmMatchConstants.NXM_1_CLASS, buffer.readUnsignedShort());
        short fieldMask = buffer.readUnsignedByte();
        assertEquals(NXM_FIELD_CODE, fieldMask >> 1);
        assertEquals(0, fieldMask & 1);
        assertEquals(VALUE_LENGTH, buffer.readUnsignedByte());
        assertEquals(0, buffer.readLong());
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);

        input = tunIdCodec.deserialize(buffer);

        TunIdCaseValue result = ((TunIdCaseValue) input.getMatchEntryValue());

        assertEquals(Nxm1Class.class, input.getOxmClass());
        assertEquals(NxmNxTunId.class, input.getOxmMatchField());
        assertEquals(false, input.isHasMask());
        assertEquals(0, result.getTunIdValues().getValue().longValue());
    }



    private MatchEntry createMatchEntry() {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        TunIdCaseValueBuilder caseBuilder = new TunIdCaseValueBuilder();
        TunIdValuesBuilder valuesBuilder = new TunIdValuesBuilder();

        matchEntryBuilder.setOxmClass(Nxm1Class.class);
        matchEntryBuilder.setOxmMatchField(NxmNxTunId.class);
        matchEntryBuilder.setHasMask(false);

        byte[] value = new byte[VALUE_LENGTH];
        valuesBuilder.setValue(new BigInteger(value));

        caseBuilder.setTunIdValues(valuesBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        return matchEntryBuilder.build();
    }

    private void createBuffer(ByteBuf message) {
        message.writeShort(OxmMatchConstants.NXM_1_CLASS);

        int fieldMask = (NXM_FIELD_CODE << 1);
        message.writeByte(fieldMask);
        message.writeByte(VALUE_LENGTH);
        byte[] value = new byte[VALUE_LENGTH];
        message.writeLong(new BigInteger(value).longValue());
    }

}