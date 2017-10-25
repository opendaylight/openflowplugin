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
import org.opendaylight.openflowplugin.impl.protocol.serialization.AbstractSerializerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.request.multipart.request.body.MultipartRequestPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.request.multipart.request.body.MultipartRequestPortStatsBuilder;

public class MultipartRequestPortStatsSerializerTest extends AbstractSerializerTest {
    private static final byte PADDING_IN_MULTIPART_REQUEST_PORTSTATS_BODY = 4;
    private static final long PORT = 42;
    private static final MultipartRequestPortStats BODY = new MultipartRequestPortStatsBuilder()
            .setNodeConnectorId(new NodeConnectorId("openflow:1:" + PORT))
            .build();

    private MultipartRequestPortStatsSerializer serializer;

    @Override
    protected void init() {
        serializer = getRegistry().getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID,
                MultipartRequestPortStats.class));
    }

    @Test
    public void testSerialize() throws Exception {
        final ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(BODY, out);

        assertEquals(out.readUnsignedInt(), PORT);
        out.skipBytes(PADDING_IN_MULTIPART_REQUEST_PORTSTATS_BODY);
        assertEquals(out.readableBytes(), 0);
    }

}