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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TunnelId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TunnelIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.tunnel.id._case.TunnelIdBuilder;

/**
 * Unit tests for OxmTunnelIdSerializer.
 *
 * @author michal.polkorab
 */
public class OxmTunnelIdSerializerTest {

    OxmTunnelIdSerializer serializer = new OxmTunnelIdSerializer();

    /**
     * Test correct serialization.
     */
    @Test
    public void testSerializeWithoutMask() {
        MatchEntryBuilder builder = prepareMatchEntry(false, new byte[]{0, 1, 2, 3, 4, 5, 6, 7});

        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(builder.build(), buffer);

        checkHeader(buffer, false);
        byte[] address = new byte[8];
        buffer.readBytes(address);
        Assert.assertArrayEquals("Wrong address", new byte[]{0, 1, 2, 3, 4, 5, 6, 7}, address);
        assertTrue("Unexpected data", buffer.readableBytes() == 0);
    }

    /**
     * Test correct serialization.
     */
    @Test
    public void testSerializeWithMask() {
        MatchEntryBuilder builder = prepareMatchEntry(true, new byte[]{8, 9, 10, 11, 12, 13, 14, 15});

        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(builder.build(), buffer);

        checkHeader(buffer, true);

        byte[] address = new byte[8];
        buffer.readBytes(address);
        Assert.assertArrayEquals("Wrong address", new byte[]{8, 9, 10, 11, 12, 13, 14, 15}, address);
        byte[] tmp = new byte[8];
        buffer.readBytes(tmp);
        Assert.assertArrayEquals("Wrong mask", new byte[]{30, 30, 25, 25, 15, 15, 0, 0}, tmp);
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
        assertEquals("Wrong oxm-class", OxmMatchConstants.TUNNEL_ID, serializer.getOxmFieldCode());
    }

    /**
     * Test correct value length return value.
     */
    @Test
    public void testGetValueLength() {
        assertEquals("Wrong value length", Long.BYTES, serializer.getValueLength());
    }

    private static MatchEntryBuilder prepareMatchEntry(boolean hasMask, byte[] value) {
        final MatchEntryBuilder builder = prepareHeader(hasMask);
        TunnelIdCaseBuilder casebuilder = new TunnelIdCaseBuilder();
        TunnelIdBuilder valueBuilder = new TunnelIdBuilder();
        if (hasMask) {
            valueBuilder.setMask(new byte[]{30, 30, 25, 25, 15, 15, 0, 0});
        }
        valueBuilder.setTunnelId(value);
        casebuilder.setTunnelId(valueBuilder.build());
        builder.setMatchEntryValue(casebuilder.build());
        return builder;
    }

    private static MatchEntryBuilder prepareHeader(boolean hasMask) {
        MatchEntryBuilder builder = new MatchEntryBuilder();
        builder.setOxmClass(OpenflowBasicClass.class);
        builder.setOxmMatchField(TunnelId.class);
        builder.setHasMask(hasMask);
        return builder;
    }

    private static void checkHeader(ByteBuf buffer, boolean hasMask) {
        assertEquals("Wrong oxm-class", OxmMatchConstants.OPENFLOW_BASIC_CLASS, buffer.readUnsignedShort());
        short fieldAndMask = buffer.readUnsignedByte();
        assertEquals("Wrong oxm-field", OxmMatchConstants.TUNNEL_ID, fieldAndMask >>> 1);
        assertEquals("Wrong hasMask", hasMask, (fieldAndMask & 1) != 0);
        if (hasMask) {
            assertEquals("Wrong length", 2 * Long.BYTES, buffer.readUnsignedByte());
        } else {
            assertEquals("Wrong length", Long.BYTES, buffer.readUnsignedByte());
        }
    }
}
