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
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.udp.dst._case.UdpDstBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

/**
 * Unit tests for OxmUdpDstSerializer.
 *
 * @author michal.polkorab
 */
public class OxmUdpDstSerializerTest {

    OxmUdpDstSerializer serializer = new OxmUdpDstSerializer();

    /**
     * Test correct serialization.
     */
    @Test
    public void testSerialize() {
        MatchEntryBuilder builder = prepareMatchEntry(2048);

        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(builder.build(), buffer);

        checkHeader(buffer, false);
        assertEquals("Wrong value", 2048, buffer.readUnsignedShort());
        assertTrue("Unexpected data", buffer.readableBytes() == 0);
    }

    /**
     * Test correct header serialization.
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
        assertEquals("Wrong oxm-class", OxmMatchConstants.UDP_DST, serializer.getOxmFieldCode());
    }

    /**
     * Test correct value length return value.
     */
    @Test
    public void testGetValueLength() {
        assertEquals("Wrong value length", Short.BYTES, serializer.getValueLength());
    }

    private static MatchEntryBuilder prepareMatchEntry(int value) {
        MatchEntryBuilder builder = prepareHeader(false);
        UdpDstCaseBuilder casebuilder = new UdpDstCaseBuilder();
        UdpDstBuilder valueBuilder = new UdpDstBuilder();
        valueBuilder.setPort(new PortNumber(Uint16.valueOf(value)));
        casebuilder.setUdpDst(valueBuilder.build());
        builder.setMatchEntryValue(casebuilder.build());
        return builder;
    }

    private static MatchEntryBuilder prepareHeader(boolean hasMask) {
        MatchEntryBuilder builder = new MatchEntryBuilder();
        builder.setOxmClass(OpenflowBasicClass.class);
        builder.setOxmMatchField(UdpDst.class);
        builder.setHasMask(hasMask);
        return builder;
    }

    private static void checkHeader(ByteBuf buffer, boolean hasMask) {
        assertEquals("Wrong oxm-class", OxmMatchConstants.OPENFLOW_BASIC_CLASS, buffer.readUnsignedShort());
        short fieldAndMask = buffer.readUnsignedByte();
        assertEquals("Wrong oxm-field", OxmMatchConstants.UDP_DST, fieldAndMask >>> 1);
        assertEquals("Wrong hasMask", hasMask, (fieldAndMask & 1) != 0);
        assertEquals("Wrong length", Short.BYTES, buffer.readUnsignedByte());
    }
}
