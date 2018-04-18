/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.packet.type.PacketType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.packet.type.PacketTypeBuilder;

public class PacketTypeEntrySerializerTest extends AbstractMatchEntrySerializerTest {

    @Test
    public void serializeTest() throws Exception {
        final int namespace = 1;
        final int ns_type = 0x894f;

        final PacketType packetType = new PacketTypeBuilder().setNamespace(namespace).setType(ns_type).build();
        final Match match = new MatchBuilder().setPacketType(packetType).build();

        assertMatch(match, false, byteBuf -> {
            assertEquals(namespace, byteBuf.readUnsignedShort());
            assertEquals(ns_type, byteBuf.readUnsignedShort());
        });
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.PACKET_TYPE;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected short getLength() {
        return EncodeConstants.SIZE_OF_SHORT_IN_BYTES * 2;
    }
}