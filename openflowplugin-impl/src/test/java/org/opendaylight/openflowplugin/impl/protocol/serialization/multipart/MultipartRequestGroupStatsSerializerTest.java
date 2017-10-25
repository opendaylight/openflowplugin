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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.request.multipart.request.body.MultipartRequestGroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.request.multipart.request.body.MultipartRequestGroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;

public class MultipartRequestGroupStatsSerializerTest extends AbstractSerializerTest {
    private static final byte PADDING_IN_MULTIPART_REQUEST_GROUP_BODY = 4;
    private static final long GROUP_ID = 42;
    private static final MultipartRequestGroupStats BODY = new MultipartRequestGroupStatsBuilder()
            .setGroupId(new GroupId(GROUP_ID))
            .build();

    private MultipartRequestGroupStatsSerializer serializer;

    @Override
    protected void init() {
        serializer = getRegistry().getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID,
                MultipartRequestGroupStats.class)) ;
    }

    @Test
    public void testSerialize() throws Exception {
        final ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(BODY, out);

        assertEquals(out.readUnsignedInt(), GROUP_ID);
        out.skipBytes(PADDING_IN_MULTIPART_REQUEST_GROUP_BODY);
        assertEquals(out.readableBytes(), 0);
    }

}