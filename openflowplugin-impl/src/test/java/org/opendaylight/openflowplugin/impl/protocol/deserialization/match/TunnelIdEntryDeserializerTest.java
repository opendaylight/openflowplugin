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

import java.math.BigInteger;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Tunnel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

public class TunnelIdEntryDeserializerTest extends AbstractMatchEntryDeserializerTest {

    @Test
    public void deserializeEntry() throws Exception {
        final ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();
        final BigInteger tunnelId = BigInteger.valueOf(6);
        final BigInteger tunnelIdMask = BigInteger.valueOf(5);

        writeHeader(in, false);
        in.writeBytes(ByteUtil.convertBigIntegerToNBytes(tunnelId, EncodeConstants.SIZE_OF_LONG_IN_BYTES));

        Tunnel match = deserialize(in).getTunnel();
        assertArrayEquals(
            ByteUtil.convertBigIntegerToNBytes(tunnelId, EncodeConstants.SIZE_OF_LONG_IN_BYTES),
            ByteUtil.convertBigIntegerToNBytes(match.getTunnelId(), EncodeConstants.SIZE_OF_LONG_IN_BYTES));

        assertEquals(0, in.readableBytes());

        writeHeader(in, true);
        in.writeBytes(ByteUtil.convertBigIntegerToNBytes(tunnelId, EncodeConstants.SIZE_OF_LONG_IN_BYTES));
        in.writeBytes(ByteUtil.convertBigIntegerToNBytes(tunnelIdMask, EncodeConstants.SIZE_OF_LONG_IN_BYTES));

        match = deserialize(in).getTunnel();
        assertArrayEquals(
            ByteUtil.convertBigIntegerToNBytes(tunnelId, EncodeConstants.SIZE_OF_LONG_IN_BYTES),
            ByteUtil.convertBigIntegerToNBytes(match.getTunnelId(), EncodeConstants.SIZE_OF_LONG_IN_BYTES));
        assertArrayEquals(
            ByteUtil.convertBigIntegerToNBytes(tunnelIdMask, EncodeConstants.SIZE_OF_LONG_IN_BYTES),
            ByteUtil.convertBigIntegerToNBytes(match.getTunnelMask(), EncodeConstants.SIZE_OF_LONG_IN_BYTES));
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
        return EncodeConstants.SIZE_OF_LONG_IN_BYTES;
    }

}
