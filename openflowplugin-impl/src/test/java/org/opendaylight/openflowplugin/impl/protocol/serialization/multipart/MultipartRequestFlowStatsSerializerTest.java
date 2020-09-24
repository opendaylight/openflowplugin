/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.math.BigInteger;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.AbstractSerializerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.multipart.request.multipart.request.body.MultipartRequestFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.multipart.request.multipart.request.body.MultipartRequestFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.multipart.request.multipart.request.body.multipart.request.flow.stats.FlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yangtools.yang.common.Uint64;

public class MultipartRequestFlowStatsSerializerTest extends AbstractSerializerTest {
    private static final byte PADDING_IN_MULTIPART_REQUEST_FLOW_BODY_01 = 3;
    private static final byte PADDING_IN_MULTIPART_REQUEST_FLOW_BODY_02 = 4;
    private static final short TABLE_ID = 42;
    private static final BigInteger OUT_PORT = BigInteger.ONE;
    private static final long OUT_GROUP = 10;
    private static final FlowCookie COOKIE = new FlowCookie(Uint64.valueOf(8));
    private static final FlowCookie COOKIE_MASK = new FlowCookie(Uint64.TEN);
    private static final Short IP_PROTOCOL_MATCH = (short) 17;
    private static final Match MATCH = new MatchBuilder()
            .setIpMatch(new IpMatchBuilder()
                    .setIpProtocol(IP_PROTOCOL_MATCH)
                    .build())
            .build();
    private static final MultipartRequestFlowStats BODY = new MultipartRequestFlowStatsBuilder()
            .setFlowStats(new FlowStatsBuilder()
                .setTableId(TABLE_ID)
                .setOutPort(OUT_PORT)
                .setOutGroup(OUT_GROUP)
                .setCookie(COOKIE)
                .setCookieMask(COOKIE_MASK)
                .setMatch(MATCH)
                .build()).build();

    private MultipartRequestFlowStatsSerializer serializer;

    @Override
    protected void init() {
        serializer = getRegistry().getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID,
                MultipartRequestFlowStats.class)) ;
    }

    @Test
    public void testSerialize() {
        final ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(BODY, out);

        assertEquals(out.readUnsignedByte(), TABLE_ID);
        out.skipBytes(PADDING_IN_MULTIPART_REQUEST_FLOW_BODY_01);
        assertEquals(out.readUnsignedInt(), OUT_PORT.longValue());
        assertEquals(out.readUnsignedInt(), OUT_GROUP);
        out.skipBytes(PADDING_IN_MULTIPART_REQUEST_FLOW_BODY_02);
        assertEquals(out.readLong(), COOKIE.getValue().longValue());
        assertEquals(out.readLong(), COOKIE_MASK.getValue().longValue());

        int matchLength = 9;
        assertEquals(out.readShort(), 1); // OXM match type
        assertEquals(out.readUnsignedShort(), matchLength); // OXM match length

        assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
        assertEquals(out.readUnsignedByte(), OxmMatchConstants.IP_PROTO << 1);
        assertEquals(out.readUnsignedByte(), Byte.BYTES);
        assertEquals(out.readUnsignedByte(), IP_PROTOCOL_MATCH.shortValue());

        int paddingRemainder = matchLength % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            out.skipBytes(EncodeConstants.PADDING - paddingRemainder);
        }

        assertEquals(out.readableBytes(), 0);
    }

}