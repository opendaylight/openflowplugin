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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv4.src._case.Ipv4SrcBuilder;

/**
 * Unit tests for OxmIpv4SrcSerializer.
 *
 * @author michal.polkorab
 */
public class OxmIpv4SrcSerializerTest {

    OxmIpv4SrcSerializer serializer = new OxmIpv4SrcSerializer();

    /**
     * Test correct serialization.
     */
    @Test
    public void testSerializeWithoutMask() {
        MatchEntryBuilder builder = prepareMatchEntry(false, "10.0.0.1");

        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(builder.build(), buffer);

        checkHeader(buffer, false);
        byte[] address = new byte[4];
        buffer.readBytes(address);
        Assert.assertArrayEquals("Wrong address", new byte[]{10, 0, 0, 1}, address);
        assertTrue("Unexpected data", buffer.readableBytes() == 0);
    }

    /**
     * Test correct serialization.
     */
    @Test
    public void testSerializeWithMask() {
        MatchEntryBuilder builder = prepareMatchEntry(true, "120.121.122.0");

        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(builder.build(), buffer);

        checkHeader(buffer, true);

        byte[] address = new byte[4];
        buffer.readBytes(address);
        Assert.assertArrayEquals("Wrong address", new byte[]{120, 121, 122, 0}, address);
        byte[] tmp = new byte[4];
        buffer.readBytes(tmp);
        Assert.assertArrayEquals("Wrong mask", new byte[]{15, 15, 0, 0}, tmp);
        assertTrue("Unexpected data", buffer.readableBytes() == 0);
    }

    /**
     * Test correct header serialization.
     */
    @Test
    public void testSerializeHeaderWithoutMask() {
        MatchEntryBuilder builder = prepareHeader(false);

        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        serializer.serializeHeader(builder.build(), buffer);

        checkHeader(buffer, false);
        assertTrue("Unexpected data", buffer.readableBytes() == 0);
    }

    /**
     * Test correct header serialization.
     */
    @Test
    public void testSerializeHeaderWithMask() {
        MatchEntryBuilder builder = prepareHeader(true);

        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        serializer.serializeHeader(builder.build(), buffer);

        checkHeader(buffer, true);
        assertTrue("Unexpected data", buffer.readableBytes() == 0);
    }

    /**
     * Test correct oxm-class return value.
     */
    @Test
    public void testGetOxmClassCode() {
        assertEquals("Wrong oxm-class", OxmMatchConstants.OPENFLOW_BASIC_CLASS, serializer.getOxmClassCode());
    }

    /**
     * Test correct oxm-field return value.
     */
    @Test
    public void getOxmFieldCode() {
        assertEquals("Wrong oxm-class", OxmMatchConstants.IPV4_SRC, serializer.getOxmFieldCode());
    }

    /**
     * Test correct value length return value.
     */
    @Test
    public void testGetValueLength() {
        assertEquals("Wrong value length", EncodeConstants.SIZE_OF_INT_IN_BYTES, serializer.getValueLength());
    }

    private static MatchEntryBuilder prepareMatchEntry(boolean hasMask, String value) {
        final MatchEntryBuilder builder = prepareHeader(hasMask);
        Ipv4SrcCaseBuilder casebuilder = new Ipv4SrcCaseBuilder();
        Ipv4SrcBuilder valueBuilder = new Ipv4SrcBuilder();
        if (hasMask) {
            valueBuilder.setMask(new byte[]{15, 15, 0, 0});
        }
        valueBuilder.setIpv4Address(new Ipv4Address(value));
        casebuilder.setIpv4Src(valueBuilder.build());
        builder.setMatchEntryValue(casebuilder.build());
        return builder;
    }

    private static MatchEntryBuilder prepareHeader(boolean hasMask) {
        MatchEntryBuilder builder = new MatchEntryBuilder();
        builder.setOxmClass(OpenflowBasicClass.class);
        builder.setOxmMatchField(Ipv4Src.class);
        builder.setHasMask(hasMask);
        return builder;
    }

    private static void checkHeader(ByteBuf buffer, boolean hasMask) {
        assertEquals("Wrong oxm-class", OxmMatchConstants.OPENFLOW_BASIC_CLASS, buffer.readUnsignedShort());
        short fieldAndMask = buffer.readUnsignedByte();
        assertEquals("Wrong oxm-field", OxmMatchConstants.IPV4_SRC, fieldAndMask >>> 1);
        assertEquals("Wrong hasMask", hasMask, (fieldAndMask & 1) != 0);
        if (hasMask) {
            assertEquals("Wrong length", EncodeConstants.SIZE_OF_LONG_IN_BYTES, buffer.readUnsignedByte());
        } else {
            assertEquals("Wrong length", EncodeConstants.SIZE_OF_INT_IN_BYTES, buffer.readUnsignedByte());
        }
    }
}
