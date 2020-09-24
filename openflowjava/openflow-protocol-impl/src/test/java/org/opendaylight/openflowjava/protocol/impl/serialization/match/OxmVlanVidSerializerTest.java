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
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanVidCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.vlan.vid._case.VlanVidBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

/**
 * Unit tests for OxmVlanVidSerializer.
 *
 * @author michal.polkorab
 */
public class OxmVlanVidSerializerTest {

    OxmVlanVidSerializer serializer = new OxmVlanVidSerializer();

    /**
     * Test correct serialization.
     */
    @Test
    public void testSerializeWithCfiBitSet() {
        MatchEntryBuilder builder = prepareVlanVidMatchEntry(false, true);

        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(builder.build(), buffer);

        checkHeader(buffer, false);
        assertEquals("Wrong value", 4596, buffer.readUnsignedShort());
        assertTrue("Unexpected data", buffer.readableBytes() == 0);
    }

    /**
     * Test correct serialization.
     */
    @Test
    public void testSerializeWithoutCfiBitSet() {
        MatchEntryBuilder builder = prepareVlanVidMatchEntry(true, false);

        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(builder.build(), buffer);

        checkHeader(buffer, true);
        assertEquals("Wrong value", 500, buffer.readUnsignedShort());
        byte[] tmp = new byte[2];
        buffer.readBytes(tmp);
        Assert.assertArrayEquals("Wrong mask", new byte[]{15, 15}, tmp);
        assertTrue("Unexpected data", buffer.readableBytes() == 0);
    }

    /**
     * Test correct header serialization.
     */
    @Test
    public void testSerializeHeaderWithoutMask() {
        MatchEntryBuilder builder = prepareVlanVidHeader(false);

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
        MatchEntryBuilder builder = prepareVlanVidHeader(true);

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
        assertEquals("Wrong oxm-class", OxmMatchConstants.VLAN_VID, serializer.getOxmFieldCode());
    }

    /**
     * Test correct value length return value.
     */
    @Test
    public void testGetValueLength() {
        assertEquals("Wrong value length", Short.BYTES, serializer.getValueLength());
    }

    private static MatchEntryBuilder prepareVlanVidMatchEntry(boolean hasMask, boolean cfiBit) {
        final MatchEntryBuilder builder = prepareVlanVidHeader(hasMask);
        final VlanVidCaseBuilder casebuilder = new VlanVidCaseBuilder();
        VlanVidBuilder valueBuilder = new VlanVidBuilder();
        if (hasMask) {
            valueBuilder.setMask(new byte[]{15, 15});
        }
        valueBuilder.setVlanVid(Uint16.valueOf(500));
        valueBuilder.setCfiBit(cfiBit);
        casebuilder.setVlanVid(valueBuilder.build());
        builder.setMatchEntryValue(casebuilder.build());
        return builder;
    }

    private static MatchEntryBuilder prepareVlanVidHeader(boolean hasMask) {
        MatchEntryBuilder builder = new MatchEntryBuilder();
        builder.setOxmClass(OpenflowBasicClass.class);
        builder.setOxmMatchField(VlanVid.class);
        builder.setHasMask(hasMask);
        return builder;
    }

    private static void checkHeader(ByteBuf buffer, boolean hasMask) {
        assertEquals("Wrong oxm-class", OxmMatchConstants.OPENFLOW_BASIC_CLASS, buffer.readUnsignedShort());
        short fieldAndMask = buffer.readUnsignedByte();
        assertEquals("Wrong oxm-field", OxmMatchConstants.VLAN_VID, fieldAndMask >>> 1);
        assertEquals("Wrong hasMask", hasMask, (fieldAndMask & 1) != 0);
        if (hasMask) {
            assertEquals("Wrong length", Integer.BYTES, buffer.readUnsignedByte());
        } else {
            assertEquals("Wrong length", Short.BYTES, buffer.readUnsignedByte());
        }
    }
}
