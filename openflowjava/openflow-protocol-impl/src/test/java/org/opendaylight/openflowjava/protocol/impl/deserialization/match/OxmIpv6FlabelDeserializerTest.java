/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Flabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6FlabelCase;

/**
 * Unit tests for OxmIpv6FlabelDeserializer.
 *
 * @author michal.polkorab
 */
public class OxmIpv6FlabelDeserializerTest {

    /**
     * Tests {@link OxmIpv6FlabelDeserializer#deserialize(ByteBuf)}.
     */
    @Test
    public void test() {
        ByteBuf buffer = BufferHelper.buildBuffer("80 00 38 04 00 00 00 02");

        buffer.skipBytes(4); // skip XID
        OxmIpv6FlabelDeserializer deserializer = new OxmIpv6FlabelDeserializer();
        MatchEntry entry = deserializer.deserialize(buffer);

        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry.getOxmClass());
        Assert.assertEquals("Wrong entry field", Ipv6Flabel.class, entry.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry.getHasMask());
        Assert.assertEquals("Wrong entry value", 2,
                ((Ipv6FlabelCase) entry.getMatchEntryValue()).getIpv6Flabel()
                .getIpv6Flabel().getValue().intValue());
    }

    /**
     * Tests {@link OxmIpv6FlabelDeserializer#deserialize(ByteBuf)}.
     */
    @Test
    public void testWithMask() {
        ByteBuf buffer = BufferHelper.buildBuffer("80 00 39 08 00 00 00 02 00 00 00 05");

        buffer.skipBytes(4); // skip XID
        OxmIpv6FlabelDeserializer deserializer = new OxmIpv6FlabelDeserializer();
        MatchEntry entry = deserializer.deserialize(buffer);

        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry.getOxmClass());
        Assert.assertEquals("Wrong entry field", Ipv6Flabel.class, entry.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", true, entry.getHasMask());
        Assert.assertEquals("Wrong entry value", 2,
                ((Ipv6FlabelCase) entry.getMatchEntryValue()).getIpv6Flabel()
                .getIpv6Flabel().getValue().intValue());
        Assert.assertArrayEquals("Wrong entry mask", new byte[]{0, 0, 0, 5},
                ((Ipv6FlabelCase) entry.getMatchEntryValue()).getIpv6Flabel().getMask());
    }
}
