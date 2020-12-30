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
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MetadataCase;

/**
 * Unit tests for OxmMetadataDeserializer.
 *
 * @author michal.polkorab
 */
public class OxmMetadataDeserializerTest {

    /**
     * Tests {@link OxmMetadataDeserializer#deserialize(ByteBuf)}.
     */
    @Test
    public void test() {
        ByteBuf buffer = BufferHelper.buildBuffer("80 00 04 08 00 00 00 00 00 00 00 03");

        buffer.skipBytes(4); // skip XID
        OxmMetadataDeserializer deserializer = new OxmMetadataDeserializer();
        MatchEntry entry = deserializer.deserialize(buffer);

        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry.getOxmClass());
        Assert.assertEquals("Wrong entry field", Metadata.class, entry.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry.getHasMask());
        Assert.assertArrayEquals("Wrong entry value", ByteBufUtils.hexStringToBytes("00 00 00 00 00 00 00 03"),
                ((MetadataCase) entry.getMatchEntryValue()).getMetadata().getMetadata());
    }
}
