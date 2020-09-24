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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpDscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpDscpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ip.dscp._case.IpDscpBuilder;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for OxmIpDscpSerializer.
 *
 * @author michal.polkorab
 */
public class OxmIpDscpSerializerTest {

    OxmIpDscpSerializer serializer = new OxmIpDscpSerializer();

    /**
     * Test correct serialization.
     */
    @Test
    public void testSerialize() {
        MatchEntryBuilder builder = prepareIpDscpMatchEntry(Uint8.valueOf(58));

        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(builder.build(), buffer);

        checkHeader(buffer, false);
        assertEquals("Wrong value", 58, buffer.readUnsignedByte());
        assertTrue("Unexpected data", buffer.readableBytes() == 0);
    }

    /**
     * Test correct header serialization.
     */
    @Test
    public void testSerializeHeader() {
        MatchEntryBuilder builder = prepareIpDscpHeader(false);

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
        assertEquals("Wrong oxm-class", OxmMatchConstants.IP_DSCP, serializer.getOxmFieldCode());
    }

    /**
     * Test correct value length return value.
     */
    @Test
    public void testGetValueLength() {
        assertEquals("Wrong value length", Byte.BYTES, serializer.getValueLength());
    }

    private static MatchEntryBuilder prepareIpDscpMatchEntry(Uint8 value) {
        MatchEntryBuilder builder = prepareIpDscpHeader(false);
        IpDscpCaseBuilder casebuilder = new IpDscpCaseBuilder();
        IpDscpBuilder dscpBuilder = new IpDscpBuilder();
        dscpBuilder.setDscp(new Dscp(value));
        casebuilder.setIpDscp(dscpBuilder.build());
        builder.setMatchEntryValue(casebuilder.build());
        return builder;
    }

    private static MatchEntryBuilder prepareIpDscpHeader(boolean hasMask) {
        MatchEntryBuilder builder = new MatchEntryBuilder();
        builder.setOxmClass(OpenflowBasicClass.class);
        builder.setOxmMatchField(IpDscp.class);
        builder.setHasMask(hasMask);
        return builder;
    }

    private static void checkHeader(ByteBuf buffer, boolean hasMask) {
        assertEquals("Wrong oxm-class", OxmMatchConstants.OPENFLOW_BASIC_CLASS, buffer.readUnsignedShort());
        short fieldAndMask = buffer.readUnsignedByte();
        assertEquals("Wrong oxm-field", OxmMatchConstants.IP_DSCP, fieldAndMask >>> 1);
        assertEquals("Wrong hasMask", hasMask, (fieldAndMask & 1) != 0);
        assertEquals("Wrong length", Byte.BYTES, buffer.readUnsignedByte());
    }
}
