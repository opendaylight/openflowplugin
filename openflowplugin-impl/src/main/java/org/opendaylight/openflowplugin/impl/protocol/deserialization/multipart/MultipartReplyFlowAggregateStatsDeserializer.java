/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.multipart.reply.multipart.reply.body.MultipartReplyFlowAggregateStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;

public class MultipartReplyFlowAggregateStatsDeserializer implements OFDeserializer<MultipartReplyBody> {

    private static final byte PADDING_IN_AGGREGATE_HEADER = 4;

    @Override
    public MultipartReplyBody deserialize(ByteBuf message) {
        final byte[] packetCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        message.readBytes(packetCount);
        final byte[] byteCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        message.readBytes(byteCount);

        final MultipartReplyFlowAggregateStatsBuilder builder = new MultipartReplyFlowAggregateStatsBuilder()
            .setPacketCount(new Counter64(new BigInteger(1, packetCount)))
            .setByteCount(new Counter64(new BigInteger(1, byteCount)))
            .setFlowCount(new Counter32(message.readUnsignedInt()));

        message.skipBytes(PADDING_IN_AGGREGATE_HEADER);
        return builder.build();
    }

}
