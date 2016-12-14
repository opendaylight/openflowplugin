/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;

public class VlanVidEntryDeserializerTest extends AbstractMatchEntryDeserializerTest {

    @Test
    public void deserializeEntry() throws Exception {
        final ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();
        final int vlanVid = 8;
        final byte[] vlanMask = new byte[] { 16, 0 };

        writeHeader(in, false);
        in.writeShort(vlanVid | (1 << 12));

        VlanMatch vlanMatch = deserialize(in).getVlanMatch();
        assertEquals(vlanVid, vlanMatch.getVlanId().getVlanId().getValue().intValue());
        assertTrue(vlanMatch.getVlanId().isVlanIdPresent());
        assertEquals(0, in.readableBytes());

        writeHeader(in, true);
        in.writeShort(vlanVid);
        in.writeBytes(vlanMask);

        vlanMatch = deserialize(in).getVlanMatch();
        assertEquals(0, vlanMatch.getVlanId().getVlanId().getValue().intValue());
        assertTrue(vlanMatch.getVlanId().isVlanIdPresent());
        assertEquals(0, in.readableBytes());

        writeHeader(in, false);
        in.writeShort(vlanVid);

        vlanMatch = deserialize(in).getVlanMatch();
        assertEquals(vlanVid, vlanMatch.getVlanId().getVlanId().getValue().intValue());
        assertFalse(vlanMatch.getVlanId().isVlanIdPresent());

    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.VLAN_VID;
    }

    @Override
    protected int getValueLength() {
        return EncodeConstants.SIZE_OF_SHORT_IN_BYTES;
    }
}
