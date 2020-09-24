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
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.impl.protocol.serialization.AbstractSerializerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.multipart.request.multipart.request.body.MultipartRequestQueueStatsBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

public class MultipartRequestMessageSerializerTest extends AbstractSerializerTest {
    // Multipart request message constants
    private static final byte PADDING_IN_MULTIPART_REQUEST_MESSAGE = 4;
    private static final short LENGTH = 24;
    private static final Uint32 XID = Uint32.valueOf(42);
    private static final Uint8 VERSION = EncodeConstants.OF_VERSION_1_3;
    private static final boolean IS_REQUEST_MORE = false;
    private static final MultipartType MULTIPART_TYPE = MultipartType.OFPMPQUEUE;
    private static final int MESSAGE_TYPE = 18;
    private static final long QUEUE_ID = 44;
    private static final long PORT = 12;

    // Message
    private static final MultipartRequest MESSAGE = new MultipartRequestBuilder()
            .setRequestMore(IS_REQUEST_MORE)
            .setXid(XID)
            .setVersion(VERSION)
            .setMultipartRequestBody(new MultipartRequestQueueStatsBuilder()
                    .setNodeConnectorId(new NodeConnectorId("openflow:1:" + PORT))
                    .setQueueId(new QueueId(QUEUE_ID))
                    .build())
            .build();

    private MultipartRequestMessageSerializer serializer;


    @Override
    protected void init() {
        serializer = getRegistry()
                .getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, MultipartRequest.class));
    }

    @Test
    public void testSerialize() {
        final ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(MESSAGE, out);

        // Header
        assertEquals(out.readByte(), VERSION);
        assertEquals(out.readByte(), MESSAGE_TYPE);
        assertEquals(out.readUnsignedShort(), LENGTH);
        assertEquals(out.readInt(), XID.intValue());
        assertEquals(out.readShort(), MULTIPART_TYPE.getIntValue());
        assertEquals(out.readUnsignedShort(), ByteBufUtils.fillBitMask(0, IS_REQUEST_MORE));
        out.skipBytes(PADDING_IN_MULTIPART_REQUEST_MESSAGE);

        // Body
        assertEquals(out.readUnsignedInt(), PORT);
        assertEquals(out.readUnsignedInt(), QUEUE_ID);

        assertEquals(out.readableBytes(), 0);
    }

}