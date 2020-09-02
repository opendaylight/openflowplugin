/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;

public class VlanPcpEntryDeserializerTest extends AbstractMatchEntryDeserializerTest {
    @Test
    public void deserializeEntry() {
        final ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();
        final short vlanPcp = 7;

        writeHeader(in, false);
        in.writeByte(vlanPcp);

        assertEquals(vlanPcp, deserialize(in).getVlanMatch().getVlanPcp().getValue().shortValue());
        assertEquals(0, in.readableBytes());
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.VLAN_PCP;
    }

    @Override
    protected int getValueLength() {
        return Byte.BYTES;
    }
}
