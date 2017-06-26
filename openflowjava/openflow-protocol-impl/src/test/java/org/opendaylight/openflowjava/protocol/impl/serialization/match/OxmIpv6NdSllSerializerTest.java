/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.serialization.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdSll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdSllCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.nd.sll._case.Ipv6NdSllBuilder;

/**
 * @author michal.polkorab
 *
 */
public class OxmIpv6NdSllSerializerTest {

    OxmIpv6NdSllSerializer serializer = new OxmIpv6NdSllSerializer();

    /**
     * Test correct serialization
     */
    @Test
    public void testSerialize() {
        MatchEntryBuilder builder = prepareMatchEntry("00:01:02:03:04:05");

        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(builder.build(), buffer);

        checkHeader(buffer, false);
        byte[] address = new byte[6];
        buffer.readBytes(address);
        Assert.assertArrayEquals("Wrong address", new byte[]{0, 1, 2, 3, 4, 5}, address);
        assertTrue("Unexpected data", buffer.readableBytes() == 0);
    }

    /**
     * Test correct header serialization
     */
    @Test
    public void testSerializeHeader() {
        MatchEntryBuilder builder = prepareHeader(false);

        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        serializer.serializeHeader(builder.build(), buffer);

        checkHeader(buffer, false);
        assertTrue("Unexpected data", buffer.readableBytes() == 0);
    }

    /**
     * Test correct oxm-class return value
     */
    @Test
    public void testGetOxmClassCode() {
        assertEquals("Wrong oxm-class", OxmMatchConstants.OPENFLOW_BASIC_CLASS, serializer.getOxmClassCode());
    }

    /**
     * Test correct oxm-field return value
     */
    @Test
    public void getOxmFieldCode() {
        assertEquals("Wrong oxm-class", OxmMatchConstants.IPV6_ND_SLL, serializer.getOxmFieldCode());
    }

    /**
     * Test correct value length return value
     */
    @Test
    public void testGetValueLength() {
        assertEquals("Wrong value length", EncodeConstants.MAC_ADDRESS_LENGTH, serializer.getValueLength());
    }

    private static MatchEntryBuilder prepareMatchEntry(String value) {
        MatchEntryBuilder builder = prepareHeader(false);
        Ipv6NdSllCaseBuilder casebuilder = new Ipv6NdSllCaseBuilder();
        Ipv6NdSllBuilder valueBuilder = new Ipv6NdSllBuilder();
        valueBuilder.setMacAddress(new MacAddress(value));
        casebuilder.setIpv6NdSll(valueBuilder.build());
        builder.setMatchEntryValue(casebuilder.build());
        return builder;
    }

    private static MatchEntryBuilder prepareHeader(boolean hasMask) {
        MatchEntryBuilder builder = new MatchEntryBuilder();
        builder.setOxmClass(OpenflowBasicClass.class);
        builder.setOxmMatchField(Ipv6NdSll.class);
        builder.setHasMask(hasMask);
        return builder;
    }

    private static void checkHeader(ByteBuf buffer, boolean hasMask) {
        assertEquals("Wrong oxm-class", OxmMatchConstants.OPENFLOW_BASIC_CLASS, buffer.readUnsignedShort());
        short fieldAndMask = buffer.readUnsignedByte();
        assertEquals("Wrong oxm-field", OxmMatchConstants.IPV6_ND_SLL, fieldAndMask >>> 1);
        assertEquals("Wrong hasMask", hasMask, (fieldAndMask & 1) != 0);
        assertEquals("Wrong length", EncodeConstants.MAC_ADDRESS_LENGTH, buffer.readUnsignedByte());
    }
}
