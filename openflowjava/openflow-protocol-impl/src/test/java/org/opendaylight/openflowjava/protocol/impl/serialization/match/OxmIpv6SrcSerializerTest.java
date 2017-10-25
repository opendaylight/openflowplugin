/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others. All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.src._case.Ipv6SrcBuilder;

/**
 * @author michal.polkorab
 *
 */
public class OxmIpv6SrcSerializerTest {

    OxmIpv6SrcSerializer serializer = new OxmIpv6SrcSerializer();

    /**
     * Test correct serialization
     */
    @Test
    public void testSerializeWithoutMask() {
        MatchEntryBuilder builder = prepareMatchEntry(false, "aaaa:bbbb:1111:2222::");

        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(builder.build(), buffer);

        checkHeader(buffer, false);
        Assert.assertEquals("Wrong ipv6 address", 43690, buffer.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 48059, buffer.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 4369, buffer.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 8738, buffer.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, buffer.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, buffer.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, buffer.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, buffer.readUnsignedShort());
        assertTrue("Unexpected data", buffer.readableBytes() == 0);
    }

    /**
     * Test correct serialization
     */
    @Test
    public void testSerialize() {
        MatchEntryBuilder builder = prepareMatchEntry(false, "::aaaa:bbbb:1111:2222");

        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(builder.build(), buffer);

        checkHeader(buffer, false);
        Assert.assertEquals("Wrong ipv6 address", 0, buffer.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, buffer.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, buffer.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, buffer.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 43690, buffer.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 48059, buffer.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 4369, buffer.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 8738, buffer.readUnsignedShort());
        assertTrue("Unexpected data", buffer.readableBytes() == 0);
    }

    private static MatchEntryBuilder prepareMatchEntry(boolean hasMask, String value) {
        MatchEntryBuilder builder = prepareHeader(hasMask);
        Ipv6SrcCaseBuilder caseBuilder = new Ipv6SrcCaseBuilder();
        Ipv6SrcBuilder srcBuilder = new Ipv6SrcBuilder();
        srcBuilder.setIpv6Address(new Ipv6Address(value));
        if (hasMask) {
            srcBuilder.setMask(new byte[]{15, 15, 0, 0});
        }
        caseBuilder.setIpv6Src(srcBuilder.build());
        builder.setMatchEntryValue(caseBuilder.build());
        return builder;
    }

    private static MatchEntryBuilder prepareHeader(boolean hasMask) {
        MatchEntryBuilder builder = new MatchEntryBuilder();
        builder.setOxmClass(OpenflowBasicClass.class);
        builder.setOxmMatchField(Ipv6Src.class);
        builder.setHasMask(hasMask);
        return builder;
    }

    private static void checkHeader(ByteBuf buffer, boolean hasMask) {
        assertEquals("Wrong oxm-class", OxmMatchConstants.OPENFLOW_BASIC_CLASS, buffer.readUnsignedShort());
        short fieldAndMask = buffer.readUnsignedByte();
        assertEquals("Wrong oxm-field", OxmMatchConstants.IPV6_SRC, fieldAndMask >>> 1);
        assertEquals("Wrong hasMask", hasMask, (fieldAndMask & 1) != 0);
        if (hasMask) {
            assertEquals("Wrong length", 2 * EncodeConstants.SIZE_OF_IPV6_ADDRESS_IN_BYTES,
                    buffer.readUnsignedByte());
        } else {
            assertEquals("Wrong length", EncodeConstants.SIZE_OF_IPV6_ADDRESS_IN_BYTES, buffer.readUnsignedByte());
        }
    }
}
