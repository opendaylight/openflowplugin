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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.multipart.request.multipart.request.body.MultipartRequestDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;

public class MultipartRequestMessageSerializerTest extends AbstractSerializerTest {
    // Multipart request message constants
    private static final byte PADDING_IN_MULTIPART_REQUEST_MESSAGE = 4;
    private static final short LENGTH = 16;
    private static final Long XID = 42L;
    private static final short VERSION = EncodeConstants.OF13_VERSION_ID;
    private static final boolean IS_REQUEST_MORE = false;
    private static final MultipartType MULTIPART_TYPE = MultipartType.OFPMPDESC;
    private static final int MESSAGE_TYPE = 18;

    // Message
    private static final MultipartRequest MESSAGE = new MultipartRequestBuilder()
            .setRequestMore(IS_REQUEST_MORE)
            .setXid(XID)
            .setVersion(VERSION)
            .setMultipartRequestBody(new MultipartRequestDescBuilder()
                    .build())
            .build();

    private MultipartRequestMessageSerializer serializer;


    @Override
    protected void init() {
        serializer = getRegistry().getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, MultipartRequest.class)) ;
    }

    @Test
    public void testSerialize() throws Exception {
        final ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(MESSAGE, out);

        // Header
        assertEquals(out.readByte(), VERSION);
        assertEquals(out.readByte(), MESSAGE_TYPE);
        assertEquals(out.readUnsignedShort(), LENGTH);
        assertEquals(out.readInt(), XID.intValue());

        // Body
        assertEquals(out.readShort(), MULTIPART_TYPE.getIntValue());
        assertEquals(out.readUnsignedShort(), ByteBufUtils.fillBitMask(0, IS_REQUEST_MORE));
        out.skipBytes(PADDING_IN_MULTIPART_REQUEST_MESSAGE);

        assertEquals(out.readableBytes(), 0);
    }

}