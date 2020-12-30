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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Ipv6ExthdrFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Exthdr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6ExthdrCase;

/**
 * Unit tests for OxmIpv6ExtHdrDeserializer.
 *
 * @author michal.polkorab
 */
public class OxmIpv6ExtHdrDeserializerTest {

    /**
     * Tests {@link OxmIpv6ExtHdrDeserializer#deserialize(ByteBuf)}.
     */
    @Test
    public void test() {
        ByteBuf buffer = BufferHelper.buildBuffer("80 00 4E 02 01 FF");

        buffer.skipBytes(4); // skip XID
        OxmIpv6ExtHdrDeserializer deserializer = new OxmIpv6ExtHdrDeserializer();
        MatchEntry entry = deserializer.deserialize(buffer);

        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry.getOxmClass());
        Assert.assertEquals("Wrong entry field", Ipv6Exthdr.class, entry.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry.getHasMask());
        Assert.assertEquals("Wrong entry value",
                new Ipv6ExthdrFlags(true, true, true, true, true, true, true, true, true),
                ((Ipv6ExthdrCase) entry.getMatchEntryValue()).getIpv6Exthdr().getPseudoField());
        Assert.assertEquals("Wrong entry mask", null, ((Ipv6ExthdrCase) entry.getMatchEntryValue())
                .getIpv6Exthdr().getMask());
        Assert.assertTrue("Unread data", buffer.readableBytes() == 0);
    }
}
