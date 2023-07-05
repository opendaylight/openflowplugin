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
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfEthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfEthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.EthSrcCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.EthSrcCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.eth.src.grouping.EthSrcValuesBuilder;

public class EthSrcCodecTest {
    private final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
    private final EthSrcCodec ethSrcCodec = new EthSrcCodec();

    private MatchEntry input;

    private static final int VALUE_LENGTH = 6;
    private static final int NXM_FIELD_CODE = 2;

    private static final byte[] TEST_ADDR = new byte[VALUE_LENGTH];
    private static final MacAddress TEST_ADDRESS = IetfYangUtil.macAddressFor(TEST_ADDR);

    @Test
    public void serializeTest() {
        input = createMatchEntry();
        ethSrcCodec.serialize(input, buffer);

        assertEquals(OxmMatchConstants.NXM_0_CLASS, buffer.readUnsignedShort());
        short fieldMask = buffer.readUnsignedByte();
        assertEquals(NXM_FIELD_CODE, fieldMask >> 1);
        assertEquals(0, fieldMask & 1);
        assertEquals(VALUE_LENGTH, buffer.readUnsignedByte());
        assertEquals(TEST_ADDRESS, ByteBufUtils.readIetfMacAddress(buffer));
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);

        input = ethSrcCodec.deserialize(buffer);

        final EthSrcCaseValue result = (EthSrcCaseValue) input.getMatchEntryValue();

        assertEquals(Nxm0Class.VALUE, input.getOxmClass());
        assertEquals(NxmOfEthSrc.VALUE, input.getOxmMatchField());
        assertEquals(false, input.getHasMask());
        assertEquals(TEST_ADDRESS, result.getEthSrcValues().getMacAddress());
    }

    private static MatchEntry createMatchEntry() {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        final EthSrcCaseValueBuilder caseBuilder = new EthSrcCaseValueBuilder();
        final EthSrcValuesBuilder valuesBuilder = new EthSrcValuesBuilder();

        matchEntryBuilder.setOxmClass(Nxm0Class.VALUE);
        matchEntryBuilder.setOxmMatchField(NxmOfEthDst.VALUE);
        matchEntryBuilder.setHasMask(false);


        valuesBuilder.setMacAddress(IetfYangUtil.macAddressFor(new byte[VALUE_LENGTH]));

        caseBuilder.setEthSrcValues(valuesBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static void createBuffer(final ByteBuf message) {
        message.writeShort(OxmMatchConstants.NXM_0_CLASS);

        int fieldMask = NXM_FIELD_CODE << 1;
        message.writeByte(fieldMask);
        message.writeByte(VALUE_LENGTH);
        message.writeBytes(TEST_ADDR);
    }
}
