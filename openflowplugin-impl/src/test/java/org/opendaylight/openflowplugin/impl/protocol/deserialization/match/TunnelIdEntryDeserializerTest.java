/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.math.BigInteger;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Tunnel;

public class TunnelIdEntryDeserializerTest extends AbstractMatchEntryDeserializerTest {

    @Test
    public void deserializeEntry() {
        final ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();
        final BigInteger tunnelId = BigInteger.valueOf(6);
        final BigInteger tunnelIdMask = BigInteger.valueOf(5);

        writeHeader(in, false);
        in.writeBytes(ByteUtil.convertBigIntegerToNBytes(tunnelId, Long.BYTES));

        Tunnel match = deserialize(in).getTunnel();
        assertArrayEquals(
                ByteUtil.convertBigIntegerToNBytes(tunnelId, Long.BYTES),
                ByteUtil.uint64toBytes(match.getTunnelId()));

        assertEquals(0, in.readableBytes());

        writeHeader(in, true);
        in.writeBytes(ByteUtil.convertBigIntegerToNBytes(tunnelId, Long.BYTES));
        in.writeBytes(ByteUtil.convertBigIntegerToNBytes(tunnelIdMask, Long.BYTES));

        match = deserialize(in).getTunnel();
        assertArrayEquals(
                ByteUtil.convertBigIntegerToNBytes(tunnelId, Long.BYTES),
                ByteUtil.uint64toBytes(match.getTunnelId()));
        assertArrayEquals(
                ByteUtil.convertBigIntegerToNBytes(tunnelIdMask, Long.BYTES),
                ByteUtil.uint64toBytes(match.getTunnelMask()));
        assertEquals(0, in.readableBytes());
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.TUNNEL_ID;
    }

    @Override
    protected int getValueLength() {
        return Long.BYTES;
    }

}
