/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.PacketType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.PacketTypeCase;

public class OxmPacketTypeDeserializerTest {

    @Test
    public void deserializeTest() {
        ByteBuf buffer = BufferHelper.buildBuffer("80 00 58 04 00 01 89 4f");

        buffer.skipBytes(4); // skip XID
        OxmPacketTypeDeserializer deserializer = new OxmPacketTypeDeserializer();
        MatchEntry entry = deserializer.deserialize(buffer);

        Assert.assertEquals(OpenflowBasicClass.class, entry.getOxmClass());
        Assert.assertEquals(PacketType.class, entry.getOxmMatchField());
        Assert.assertEquals(false, entry.getHasMask());

        PacketTypeCase packetTypeCase = (PacketTypeCase) entry.getMatchEntryValue();
        Assert.assertEquals(0x1894f, packetTypeCase.getPacketType().getPacketType().longValue());
    }
}