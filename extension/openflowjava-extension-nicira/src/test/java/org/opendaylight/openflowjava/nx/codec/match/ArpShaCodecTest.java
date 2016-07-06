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
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxArpSha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.arp.sha.grouping.ArpShaValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.ArpShaCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.ArpShaCaseValueBuilder;

public class ArpShaCodecTest {

    ArpShaCodec arpShaCodec;
    ByteBuf buffer;
    MatchEntry input;

    private static final int VALUE_LENGTH = 6;
    private static final int NXM_FIELD_CODE = 17;

    private static byte[] resAddress = new byte[VALUE_LENGTH];
    private static final MacAddress resultAddress = new MacAddress(ByteBufUtils.macAddressToString(resAddress));


    @Before
    public void setUp() {
        arpShaCodec = new ArpShaCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }


    @Test
    public void serializeTest() {
        input = createMatchEntry();
        arpShaCodec.serialize(input, buffer);

        assertEquals(OxmMatchConstants.NXM_1_CLASS, buffer.readUnsignedShort());
        short fieldMask = buffer.readUnsignedByte();
        assertEquals(NXM_FIELD_CODE, fieldMask >> 1);
        assertEquals(0, fieldMask & 1);
        assertEquals(VALUE_LENGTH, buffer.readUnsignedByte());
        assertEquals(resultAddress, ByteBufUtils.readIetfMacAddress(buffer));
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);

        input = arpShaCodec.deserialize(buffer);

        ArpShaCaseValue result = ((ArpShaCaseValue) input.getMatchEntryValue());

        assertEquals(Nxm1Class.class, input.getOxmClass());
        assertEquals(NxmNxArpSha.class, input.getOxmMatchField());
        assertEquals(false, input.isHasMask());
        assertEquals(resultAddress, result.getArpShaValues().getMacAddress());
    }


    private MatchEntry createMatchEntry() {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        ArpShaCaseValueBuilder caseBuilder = new ArpShaCaseValueBuilder();
        ArpShaValuesBuilder valuesBuilder = new ArpShaValuesBuilder();

        matchEntryBuilder.setOxmClass(Nxm1Class.class);
        matchEntryBuilder.setOxmMatchField(NxmNxArpSha.class);
        matchEntryBuilder.setHasMask(false);


        byte[] address = new byte[VALUE_LENGTH];

        valuesBuilder.setMacAddress(new MacAddress(ByteBufUtils.macAddressToString(address)));

        caseBuilder.setArpShaValues(valuesBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        return matchEntryBuilder.build();
    }

    private void createBuffer(ByteBuf message) {
        message.writeShort(OxmMatchConstants.NXM_1_CLASS);

        int fieldMask = (NXM_FIELD_CODE << 1);
        message.writeByte(fieldMask);
        message.writeByte(VALUE_LENGTH);
        message.writeBytes(resAddress);
    }

}
