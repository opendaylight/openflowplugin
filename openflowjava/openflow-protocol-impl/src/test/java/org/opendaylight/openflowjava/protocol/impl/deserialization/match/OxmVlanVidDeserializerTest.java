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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanVidCase;

/**
 * Unit tests for OxmVlanVidDeserializer.
 *
 * @author michal.polkorab
 */
public class OxmVlanVidDeserializerTest {

    /**
     * Tests {@link OxmVlanVidDeserializer#deserialize(ByteBuf)}.
     */
    @Test
    public void test() {
        ByteBuf buffer = BufferHelper.buildBuffer("80 00 0C 02 20 0A");

        buffer.skipBytes(4); // skip XID
        OxmVlanVidDeserializer deserializer = new OxmVlanVidDeserializer();
        MatchEntry entry = deserializer.deserialize(buffer);

        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry.getOxmClass());
        Assert.assertEquals("Wrong entry field", VlanVid.class, entry.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry.getHasMask());
        Assert.assertEquals("Wrong entry value", 10,
                ((VlanVidCase) entry.getMatchEntryValue()).getVlanVid().getVlanVid().intValue());
        Assert.assertEquals("Wrong entry value", false,
                ((VlanVidCase) entry.getMatchEntryValue()).getVlanVid().getCfiBit());
    }
}
