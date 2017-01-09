/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TunnelBuilder;

public class TunnelIdEntrySerializerTest extends AbstractMatchEntrySerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final long tunnelId = 8;
        final byte[] tcpMask = new byte[] { 30, 30, 30, 30, 0, 0, 0, 0 };

        final ByteBuffer maskBuff = ByteBuffer.allocate(Long.BYTES);
        maskBuff.put(tcpMask, 0, tcpMask.length);
        maskBuff.flip();

        final Match tcpFlagsMatch = new MatchBuilder()
                .setTunnel(new TunnelBuilder()
                        .setTunnelId(BigInteger.valueOf(tunnelId))
                        .setTunnelMask(BigInteger.valueOf(maskBuff.getLong()))
                        .build())
                .build();

        assertMatch(tcpFlagsMatch, true, (out) -> {
            assertEquals(out.readLong(), tunnelId);

            byte[] mask = new byte[8];
            out.readBytes(mask);
            assertArrayEquals(mask, tcpMask);
        });

        final Match tcpFlagsMatchNoMask = new MatchBuilder()
                .setTunnel(new TunnelBuilder()
                        .setTunnelId(BigInteger.valueOf(tunnelId))
                        .build())
                .build();

        assertMatch(tcpFlagsMatchNoMask, false, (out) -> assertEquals(out.readLong(), tunnelId));
    }

    @Override
    protected short getLength() {
        return EncodeConstants.SIZE_OF_LONG_IN_BYTES;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.TUNNEL_ID;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

}
